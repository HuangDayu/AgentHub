package com.agenthub.infrastructure.agents.ali;

import com.agenthub.domain.model.AgentToolInfo;
import com.agenthub.domain.model.AgentToolType;
import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.infrastructure.agents.AbstractReActAgent;
import com.agenthub.infrastructure.agents.ReActAgentFactory;
import com.agenthub.infrastructure.agents.ali.hook.AgentHookFactory;
import com.agenthub.infrastructure.agents.ali.interceptor.InterceptorFactory;
import com.agenthub.infrastructure.agents.ali.saver.SaverFactory;
import com.agenthub.infrastructure.agents.ali.store.StoreFactory;
import com.agenthub.infrastructure.agents.ali.tools.GraphToolsFactory;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import com.agenthub.infrastructure.tools.AgentToolsFactory;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.Builder;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.domain.model.AgentToolType.*;
import static com.agenthub.common.utils.TtlUtils.parallelStreamWithTtl;

/**
 * Agent运行时工厂，根据配置创建AgentRuntime。
 */
@RequiredArgsConstructor
@Component
public class AliReActAgentFactory implements ReActAgentFactory {

    private final SaverFactory saverFactory;
    private final InterceptorFactory interceptorFactory;
    private final AgentHookFactory agentHookFactory;
    private final StoreFactory storeFactory;
    private final SpringShareObjectFactory springShareObjectFactory;
    private final AgentToolsFactory agentToolsFactory;
    private final GraphToolsFactory graphToolsFactory;


    @Override
    public AbstractReActAgent create(ReActAgentContext reActAgentContext) {
        AliReActAgentConfig aliReActAgentConfig = buildAliReActAgentConfig(reActAgentContext);
        ReactAgent agent = buildReactAgent(aliReActAgentConfig);
        return new AliReActAgent(reActAgentContext, agent);
    }

    private AliReActAgentConfig buildAliReActAgentConfig(ReActAgentContext context) {
        return new AliReActAgentConfig(
                context.getAgentName(),
                resolveChatModel(context),
                context.getSystemPrompt(),
                resolveTools(context),
                resolveHooks(context),
                resolveInterceptors(),
                resolveSaver(),
                resolveStore(),
                Map.of(AGENT_CONTEXT_KEY, context)
        );
    }

    private ChatModel resolveChatModel(ReActAgentContext context) {
        String chatModelId = context.getChatModelId();
        if (chatModelId == null) return null;
        return springShareObjectFactory.getChatModelByConfigId(chatModelId);
    }

    private List<ToolCallback> resolveTools(ReActAgentContext context) {
        List<ToolCallback> tools = new CopyOnWriteArrayList<>();
        Map<AgentToolType, List<AgentToolInfo>> collect = context.getTools().stream().collect(Collectors.groupingBy(
                AgentToolInfo::getType,
                Collectors.mapping(v -> v, Collectors.toList())
        ));
        parallelStreamWithTtl(4, collect.entrySet(), entry -> {
            if (!entry.getValue().isEmpty()) {
                Set<ToolCallback> toolCallbacks = resolveToolCallbacks(entry.getKey(), entry.getValue());
                if (!toolCallbacks.isEmpty()) {
                    tools.addAll(toolCallbacks);
                }
            }
            return null;
        });
        tools.addAll(graphToolsFactory.getToolCallbacks(context.getWorkspace()));
        return tools;
    }


    private Set<ToolCallback> resolveToolCallbacks(AgentToolType toolInfo, List<AgentToolInfo> toolIds) {
        return switch (toolInfo) {
            case SYSTEM_TOOL -> resolveSystemTools(toolIds);
            case MCP_TOOL -> resolveMcpTools(toolIds);
            case SKILL_TOOL -> resolveSkillTools(toolIds);
            case HTTP_TOOL -> resolveHttpTools(toolIds);
        };
    }

    /**
     * 注意：SystemTools 是类级别的启用停用控制，所以这里需要根据 class name 进行过滤
     *
     * @return
     */
    private Set<ToolCallback> resolveSystemTools(List<AgentToolInfo> toolIds) {
        return agentToolsFactory.getToolCallbacks(SYSTEM_TOOL, toolIds);
    }

    private Set<ToolCallback> resolveMcpTools(List<AgentToolInfo> toolIds) {
        return agentToolsFactory.getToolCallbacks(MCP_TOOL, toolIds);
    }

    private Set<ToolCallback> resolveSkillTools(List<AgentToolInfo> toolIds) {
        return agentToolsFactory.getToolCallbacks(SKILL_TOOL, toolIds);
    }

    private Set<ToolCallback> resolveHttpTools(List<AgentToolInfo> toolIds) {
        return agentToolsFactory.getToolCallbacks(HTTP_TOOL, toolIds);
    }

    private List<ToolCallback> filterByName(Set<ToolCallback> callbacks, String name) {
        return callbacks.stream()
                .filter(cb -> cb.getToolDefinition().name().equals(name))
                .toList();
    }

    private List<com.alibaba.cloud.ai.graph.agent.hook.Hook> resolveHooks(ReActAgentContext context) {
        return List.of(agentHookFactory.loggingHook(),
                agentHookFactory.skillsAgentHook(context.getWorkspace()),
                agentHookFactory.shellToolAgentHook(context.getWorkspace()));
    }

    private List<com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor> resolveInterceptors() {
        return List.of(interceptorFactory.monitoringInterceptor());
    }

    private BaseCheckpointSaver resolveSaver() {
        return saverFactory.postgresSaver();
    }

    private DatabaseStore resolveStore() {
        return storeFactory.databaseStore();
    }


    private ReactAgent buildReactAgent(AliReActAgentConfig config) {
        Builder builder = ReactAgent.builder()
                .name(config.name())
                .model(config.chatModel())
                .toolContext(config.toolContext())
                .tools(config.tools());
        applySystemPrompt(builder, config);
        applySaver(builder, config);
        applyHooks(builder, config);
        applyInterceptors(builder, config);
        return builder.build();
    }

    private void applySystemPrompt(Builder builder, AliReActAgentConfig config) {
        if (config.systemPrompt() != null) {
            builder.systemPrompt(config.systemPrompt());
        }
    }

    private void applySaver(Builder builder, AliReActAgentConfig config) {
        if (config.saver() != null) {
            builder.saver(config.saver());
        }
    }

    private void applyHooks(Builder builder, AliReActAgentConfig config) {
        if (config.hooks() != null && !config.hooks().isEmpty()) {
            builder.hooks(config.hooks());
        }
    }

    private void applyInterceptors(Builder builder, AliReActAgentConfig config) {
        if (config.interceptors() != null && !config.interceptors().isEmpty()) {
            builder.interceptors(config.interceptors());
        }
    }

}
