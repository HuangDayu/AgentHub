package com.agenthub.infrastructure.agents.alibaba;

import com.agenthub.application.factory.ReActAgentFactory;
import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.common.utils.TtlUtils;
import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AgentToolInfo;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.ModelStrategy;
import com.agenthub.infrastructure.agents.alibaba.hook.AgentHookFactory;
import com.agenthub.infrastructure.agents.alibaba.interceptor.InterceptorFactory;
import com.agenthub.infrastructure.agents.alibaba.saver.SaverFactory;
import com.agenthub.infrastructure.agents.alibaba.store.StoreFactory;
import com.agenthub.infrastructure.agents.alibaba.tools.GraphToolsFactory;
import com.agenthub.infrastructure.context.TenantContextGetter;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import com.agenthub.infrastructure.tools.AgentToolsFactory;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.Builder;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.common.constants.AgentConstants.THREAD_CONTEXT_KEY;
import static com.agenthub.common.utils.TtlUtils.parallelStreamWithTtl;
import static com.agenthub.domain.enums.AgentToolType.*;

/**
 * Agent运行时工厂，根据配置创建AgentRuntime。
 */
@RequiredArgsConstructor
@Component
public class AlibabaReActAgentFactory implements ReActAgentFactory {

    private final SaverFactory saverFactory;
    private final InterceptorFactory interceptorFactory;
    private final AgentHookFactory agentHookFactory;
    private final StoreFactory storeFactory;
    private final SpringShareObjectFactory springShareObjectFactory;
    private final AgentToolsFactory agentToolsFactory;
    private final GraphToolsFactory graphToolsFactory;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;
    private final ObjectProvider<TeamAgentFactory> teamAgentFactoryObjectProvider;
    private final TenantContextGetter tenantContextGetter;


    @Override
    public AbstractReActAgent create(ReActAgentContext reActAgentContext) {
        AlibabaReActAgentConfig alibabaReActAgentConfig = buildAliReActAgentConfig(reActAgentContext);
        ReactAgent agent = buildReactAgent(alibabaReActAgentConfig);
        return new AlibabaReActAgent(reActAgentContext, alibabaReActAgentConfig, teamAgentFactoryObjectProvider.getObject(), agent);
    }

    private ReactAgent buildReactAgent(AlibabaReActAgentConfig config) {
        Builder builder = ReactAgent.builder()
                .name(config.getAgent().getName())
                .description(config.getAgent().getDescription())
                .toolContext(config.getToolContext())
                .tools(config.getTools())
                .chatClient(buildChatClient(config))
                .executor(TtlUtils.getTtlExecutorService())
                .maxParallelTools(5)
                .parallelToolExecution(true)
                .releaseThread(true);
        applySystemPrompt(builder, config);
        applySaver(builder, config);
        applyHooks(builder, config);
        applyInterceptors(builder, config);
        return builder.build();
    }

    private ChatClient buildChatClient(AlibabaReActAgentConfig config) {
        ChatClient.Builder builder = ChatClient.builder(config.getChatModel());
        if (config.getChatOptions() != null) {
            builder.defaultOptions(config.getChatOptions());
        }
        if (config.getAdvisors() != null && config.getAdvisors().length > 0) {
            builder.defaultAdvisors(config.getAdvisors());
        }
        return builder.build();
    }

    private AlibabaReActAgentConfig buildAliReActAgentConfig(ReActAgentContext context) {
        return new AlibabaReActAgentConfig(
                context.getAgent(),
                resolveChatModel(context),
                resolveAdvisors(context),
                resolveChatOptions(context),
                context.getSystemPrompt(),
                resolveTools(context),
                resolveHooks(context),
                resolveInterceptors(),
                resolveSaver(),
                resolveStore(),
                resolveToolsContext(context),
                resolveRunnableConfig(context)
        );
    }

    private Advisor[] resolveAdvisors(ReActAgentContext context) {
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(context.getModelStrategy().getMaxMessages()).build();
        return new Advisor[]{MessageChatMemoryAdvisor.builder(memory).conversationId(context.getSessionId()).build()};
    }

    private ChatOptions resolveChatOptions(ReActAgentContext context) {
        ModelStrategy modelStrategy = context.getModelStrategy();
        DefaultToolCallingChatOptions options = new DefaultToolCallingChatOptions();
        options.setTemperature(modelStrategy.getTemperature());
        options.setTopK(modelStrategy.getTopK());
        options.setTopP(modelStrategy.getTopP());
        options.setMaxTokens(modelStrategy.getMaxTokens());
        options.setInternalToolExecutionEnabled(true);
        return options;
    }

    private RunnableConfig resolveRunnableConfig(ReActAgentContext context) {
        return RunnableConfig.builder()
                .addMetadata(AGENT_CONTEXT_KEY, context)
                .addMetadata(THREAD_CONTEXT_KEY, tenantContextGetter.findTenantThreadContext().orElse(null))
                .threadId(context.getSessionId()).build();
    }

    private Map<String, Object> resolveToolsContext(ReActAgentContext context) {
        Map<String, Object> map = new HashMap<>();
        map.put(AGENT_CONTEXT_KEY, context);
        map.put(THREAD_CONTEXT_KEY, tenantContextGetter.findTenantThreadContext().orElse(null));
        map.put(ChatMemory.CONVERSATION_ID, context.getAgent().getId());
        return map;
    }

    private ChatModel resolveChatModel(ReActAgentContext context) {
        String chatModelId = context.getChatModelId();
        if (chatModelId == null) return null;
        return springShareObjectFactory.getChatModelByConfigId(chatModelId);
    }

    private List<ToolCallback> resolveTools(ReActAgentContext context) {
        List<ToolCallback> tools = new CopyOnWriteArrayList<>();
        var collect = context.getTools().stream().collect(Collectors.groupingBy(AgentToolInfo::getType));
        parallelStreamWithTtl(4, collect.entrySet(), entry -> {
            if (!entry.getValue().isEmpty()) {
                var toolCallbacks = resolveToolCallbacks(entry.getKey(), entry.getValue());
                if (!toolCallbacks.isEmpty()) tools.addAll(toolCallbacks);
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

    private void applySystemPrompt(Builder builder, AlibabaReActAgentConfig config) {
        if (config.getSystemPrompt() != null) {
            builder.systemPrompt(config.getSystemPrompt());
        }
    }

    private void applySaver(Builder builder, AlibabaReActAgentConfig config) {
        if (config.getSaver() != null) {
            builder.saver(config.getSaver());
        }
    }

    private void applyHooks(Builder builder, AlibabaReActAgentConfig config) {
        if (config.getHooks() != null && !config.getHooks().isEmpty()) {
            builder.hooks(config.getHooks());
        }
    }

    private void applyInterceptors(Builder builder, AlibabaReActAgentConfig config) {
        if (config.getInterceptors() != null && !config.getInterceptors().isEmpty()) {
            builder.interceptors(config.getInterceptors());
        }
    }

}
