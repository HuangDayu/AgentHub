package com.agenthub.infrastructure.agents.ali;

import com.agenthub.domain.model.AgentToolInfo;
import com.agenthub.infrastructure.agents.AbstractReActAgent;
import com.agenthub.infrastructure.agents.ReActAgentContext;
import com.agenthub.infrastructure.agents.ReActAgentFactory;
import com.agenthub.infrastructure.agents.ali.hook.AgentHookFactory;
import com.agenthub.infrastructure.agents.ali.interceptor.InterceptorFactory;
import com.agenthub.infrastructure.agents.ali.saver.SaverFactory;
import com.agenthub.infrastructure.agents.ali.store.StoreFactory;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import com.agenthub.infrastructure.tools.AgentToolsFactory;
import com.alibaba.cloud.ai.graph.agent.Builder;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.agenthub.domain.model.AgentToolType.*;

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
                resolveHooks(),
                resolveInterceptors(),
                resolveSaver(),
                resolveStore()
        );
    }

    private ChatModel resolveChatModel(ReActAgentContext context) {
        String chatModelId = context.getChatModelId();
        if (chatModelId == null) return null;
        return springShareObjectFactory.getChatModelByConfigId(chatModelId);
    }

    private List<ToolCallback> resolveTools(ReActAgentContext context) {
        List<AgentToolInfo> toolInfos = context.getTools();
        if (toolInfos == null || toolInfos.isEmpty()) return new ArrayList<>();

        Set<String> uniqueNames = new java.util.HashSet<>();
        List<ToolCallback> tools = new ArrayList<>();

        for (AgentToolInfo toolInfo : toolInfos) {
            processToolInfo(toolInfo, uniqueNames, tools);
        }
        return tools;
    }

    private void processToolInfo(AgentToolInfo toolInfo, Set<String> uniqueNames, List<ToolCallback> tools) {
        if (!toolInfo.isEnabled()) return;
        if (uniqueNames.contains(toolInfo.getName())) return;

        List<ToolCallback> callbacks = resolveToolCallbacks(toolInfo);
        if (!callbacks.isEmpty()) {
            uniqueNames.add(toolInfo.getName());
            tools.addAll(callbacks);
        }
    }

    private List<ToolCallback> resolveToolCallbacks(AgentToolInfo toolInfo) {
        return switch (toolInfo.getType()) {
            case FUNCTION_TOOLS -> resolveFunctionTools(toolInfo);
            case MCP_TOOLS -> resolveMcpTools(toolInfo);
            case SKILL_TOOLS -> resolveSkillTools(toolInfo);
            case HTTP_TOOLS -> resolveHttpTools(toolInfo);
        };
    }

    /**
     * 注意：FunctionTools 是类级别的启用停用控制，所以这里需要根据 class name 进行过滤
     *
     * @param toolInfo
     * @return
     * @see com.agenthub.infrastructure.tools.function_tools.FunctionToolScanner
     */
    private List<ToolCallback> resolveFunctionTools(AgentToolInfo toolInfo) {
        Set<ToolCallback> callbacks = agentToolsFactory.getToolCallbacks(FUNCTION_TOOLS);
        return callbacks.stream().filter(callback -> callback.getClass().getName().equals(toolInfo.getName())).toList();
    }

    private List<ToolCallback> resolveMcpTools(AgentToolInfo toolInfo) {
        Set<ToolCallback> callbacks = agentToolsFactory.getToolCallbacks(MCP_TOOLS);
        return filterByName(callbacks, toolInfo.getName());
    }

    private List<ToolCallback> resolveSkillTools(AgentToolInfo toolInfo) {
        Set<ToolCallback> callbacks = agentToolsFactory.getToolCallbacks(SKILL_TOOLS);
        return filterByName(callbacks, toolInfo.getName());
    }

    private List<ToolCallback> resolveHttpTools(AgentToolInfo toolInfo) {
        Set<ToolCallback> callbacks = agentToolsFactory.getToolCallbacks(HTTP_TOOLS);
        return filterByName(callbacks, toolInfo.getName());
    }

    private List<ToolCallback> filterByName(Set<ToolCallback> callbacks, String name) {
        return callbacks.stream()
                .filter(cb -> cb.getToolDefinition().name().equals(name))
                .toList();
    }

    private List<com.alibaba.cloud.ai.graph.agent.hook.Hook> resolveHooks() {
        return List.of(agentHookFactory.loggingHook());
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
                .tools(new ArrayList<>(config.tools()));
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
