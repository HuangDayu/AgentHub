package com.agenthub.infrastructure.agents.ali;

import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.infrastructure.agents.AbstractReActAgent;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

/**
 * 单个Agent运行时，封装ReactAgent的创建与执行。
 */
public class AliReActAgent extends AbstractReActAgent {

    private final ReActAgentContext context;
    private final ReactAgent agent;

    public AliReActAgent(ReActAgentContext context, ReactAgent agent) {
        this.context = context;
        this.agent = agent;
    }

    public String getName() {
        return context.getAgentName();
    }

    @Override
    public ReActAgentContext getContext() {
        return context;
    }

    @SneakyThrows
    @Override
    public Flux<Message> streamMessages(String userMessage) {
        return agent.streamMessages(userMessage);
    }

    @SneakyThrows
    @Override
    public AssistantMessage call(String userMessage) {
        return agent.call(userMessage);
    }

    @Override
    public void interrupt() {
        agent.interrupt(RunnableConfig.builder().build());
    }
}
