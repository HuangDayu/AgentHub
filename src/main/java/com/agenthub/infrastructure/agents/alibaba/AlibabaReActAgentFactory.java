package com.agenthub.infrastructure.agents.alibaba;

import com.agenthub.application.factory.ReActAgentFactory;
import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.common.utils.TtlUtils;
import com.agenthub.domain.model.agent.AbstractReActAgent;
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

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.common.constants.AgentConstants.THREAD_CONTEXT_KEY;

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
    public AbstractReActAgent create(ReActAgentContext ctx) {
        AlibabaReActAgentConfig config = buildAliReActAgentConfig(ctx);
        ReactAgent agent = buildReactAgent(config);
        return new AlibabaReActAgent(ctx, config,
                teamAgentFactoryObjectProvider.getObject(), agent);
    }

    private ReactAgent buildReactAgent(AlibabaReActAgentConfig config) {
        var builder = createBaseBuilder(config);
        applySystemPrompt(builder, config);
        applySaver(builder, config);
        applyHooks(builder, config);
        applyInterceptors(builder, config);
        return builder.build();
    }

    private Builder createBaseBuilder(AlibabaReActAgentConfig config) {
        return buildBaseReactAgent(config)
                .chatClient(buildChatClient(config))
                .executor(TtlUtils.getTtlExecutorService())
                .maxParallelTools(5)
                .parallelToolExecution(true)
                .releaseThread(true);
    }

    private Builder buildBaseReactAgent(AlibabaReActAgentConfig config) {
        return ReactAgent.builder()
                .name(config.getAgent().getName())
                .description(config.getAgent().getDescription())
                .toolContext(config.getToolContext())
                .tools(config.getTools());
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
        ChatModel chatModel = resolveChatModel(context); Advisor[] advisors = resolveAdvisors(context);
        ChatOptions chatOptions = resolveChatOptions(context);
        var hooks = resolveHooks(context); var interceptors = resolveInterceptors(context);
        var saver = resolveSaver(); var store = resolveStore();
        var toolsContext = resolveToolsContext(context); var runConfig = resolveRunnableConfig(context);
        return new AlibabaReActAgentConfig(context.getAgent(), chatModel, advisors, chatOptions,
                context.getSystemPrompt(), resolveTools(context), hooks, interceptors, saver, store,
                toolsContext, runConfig);
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
        List<Object> toolCallbacks = context.getToolCallbacks();
        if (toolCallbacks != null && !toolCallbacks.isEmpty()) {
            tools.addAll(toolCallbacks.stream()
                    .filter(ToolCallback.class::isInstance)
                    .map(ToolCallback.class::cast)
                    .toList());
        }
        tools.addAll(graphToolsFactory.getToolCallbacks(context.getWorkspace()));
        return tools;
    }

    private List<com.alibaba.cloud.ai.graph.agent.hook.Hook> resolveHooks(ReActAgentContext context) {
        return List.of(agentHookFactory.loggingHook(),
                agentHookFactory.modelStrategyHook(context),
                agentHookFactory.skillsAgentHook(context.getWorkspace()),
                agentHookFactory.shellToolAgentHook(context.getWorkspace()));
    }

    private List<com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor> resolveInterceptors(ReActAgentContext context) {
        return List.of(interceptorFactory.monitoringInterceptor(),
                interceptorFactory.toolStrategyInterceptor(context));
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
