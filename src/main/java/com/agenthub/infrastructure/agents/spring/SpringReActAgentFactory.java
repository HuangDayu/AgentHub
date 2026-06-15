package com.agenthub.infrastructure.agents.spring;

import com.agenthub.application.factory.ReActAgentFactory;
import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.GuardrailStrategy;
import com.agenthub.domain.model.strategy.ModelStrategy;
import com.agenthub.domain.model.strategy.RetrievalStrategy;
import com.agenthub.domain.model.strategy.ToolStrategy;
import com.agenthub.infrastructure.agents.spring.advisor.GuardrailStrategyAdvisor;
import com.agenthub.infrastructure.agents.spring.advisor.ModelStrategyAdvisor;
import com.agenthub.infrastructure.agents.spring.advisor.RetrievalStrategyAdvisor;
import com.agenthub.infrastructure.agents.spring.advisor.ToolStrategyAdvisor;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI Agent 运行时工厂，实现 ReActAgentFactory 端口。
 */
@Component
@RequiredArgsConstructor
public class SpringReActAgentFactory implements ReActAgentFactory {

    private final SpringShareObjectFactory springShareObjectFactory;

    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;

    private final TeamAgentFactory teamAgentFactory;

    @Override
    public AbstractReActAgent create(ReActAgentContext ctx) {
        ChatClient chatClient = buildChatClient(ctx);
        return new SpringReActAgent(ctx, chatClient, teamAgentFactory);
    }

    private ChatClient buildChatClient(ReActAgentContext ctx) {
        ChatModel chatModel = resolveChatModel(ctx);
        List<Advisor> advisors = resolveAdvisors(ctx);
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultSystem(ctx.getSystemPrompt())
                .defaultAdvisors(advisors.toArray(new Advisor[0]));
        resolveTools(ctx, builder);
        return builder.build();
    }

    private ChatModel resolveChatModel(ReActAgentContext ctx) {
        String chatModelId = ctx.getChatModelId();
        if (chatModelId == null) return null;
        return springShareObjectFactory.getChatModelByConfigId(chatModelId);
    }

    private List<Advisor> resolveAdvisors(ReActAgentContext ctx) {
        List<Advisor> advisors = new ArrayList<>();
        addRetrievalAdvisor(advisors, ctx);
        addModelAdvisor(advisors, ctx);
        addToolCallAdvisor(advisors, ctx);
        addToolAdvisor(advisors, ctx);
        addMemoryAdvisor(advisors, ctx);
        addGuardrailAdvisor(advisors, ctx);
        return advisors;
    }

    private void addRetrievalAdvisor(List<Advisor> advisors, ReActAgentContext ctx) {
        RetrievalStrategy strategy = ctx.getRetrievalStrategy();
        if (strategy != null) {
            advisors.add(new RetrievalStrategyAdvisor(ctx, strategy));
        }
    }

    private void addModelAdvisor(List<Advisor> advisors, ReActAgentContext ctx) {
        ModelStrategy strategy = ctx.getModelStrategy();
        if (strategy != null) {
            advisors.add(new ModelStrategyAdvisor(ctx, strategy));
        }
    }

    private void addToolAdvisor(List<Advisor> advisors, ReActAgentContext ctx) {
        ToolStrategy strategy = ctx.getToolStrategy();
        if (strategy != null) {
            advisors.add(new ToolStrategyAdvisor(ctx, strategy));
        }
    }

    private void addToolCallAdvisor(List<Advisor> advisors, ReActAgentContext ctx) {
        List<ToolCallback> callbacks = resolveToolCallbacks(ctx);
        if (callbacks.isEmpty()) return;
        ToolCallingManager manager = DefaultToolCallingManager.builder()
                .toolCallbackResolver(new StaticToolCallbackResolver(callbacks))
                .build();
        advisors.add(ToolCallAdvisor.builder().toolCallingManager(manager).advisorOrder(10).build());
    }

    private List<ToolCallback> resolveToolCallbacks(ReActAgentContext ctx) {
        List<Object> raw = ctx.getToolCallbacks();
        if (raw == null) return List.of();
        return raw.stream()
                .filter(ToolCallback.class::isInstance)
                .map(ToolCallback.class::cast)
                .toList();
    }

    private void addMemoryAdvisor(List<Advisor> advisors, ReActAgentContext ctx) {
        ModelStrategy modelStrategy = ctx.getModelStrategy();
        if (modelStrategy == null) return;
        advisors.add(buildMemoryAdvisor(ctx, modelStrategy));
    }

    private MessageChatMemoryAdvisor buildMemoryAdvisor(ReActAgentContext ctx, ModelStrategy strategy) {
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(strategy.getMaxMessages())
                .build();
        return MessageChatMemoryAdvisor.builder(memory)
                .conversationId(ctx.getSessionId())
                .build();
    }

    private void addGuardrailAdvisor(List<Advisor> advisors, ReActAgentContext ctx) {
        GuardrailStrategy strategy = ctx.getGuardrailStrategy();
        if (strategy != null) {
            advisors.add(new GuardrailStrategyAdvisor(ctx, strategy));
        }
    }

    private void resolveTools(ReActAgentContext ctx, ChatClient.Builder builder) {
        List<Object> toolCallbacks = ctx.getToolCallbacks();
        if (toolCallbacks == null || toolCallbacks.isEmpty()) return;
        org.springframework.ai.tool.ToolCallback[] tools = toolCallbacks.stream()
                .filter(org.springframework.ai.tool.ToolCallback.class::isInstance)
                .map(org.springframework.ai.tool.ToolCallback.class::cast)
                .toArray(org.springframework.ai.tool.ToolCallback[]::new);
        builder.defaultToolCallbacks(tools);
    }
}
