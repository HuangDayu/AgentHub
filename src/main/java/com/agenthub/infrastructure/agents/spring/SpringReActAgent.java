package com.agenthub.infrastructure.agents.spring;

import com.agenthub.domain.enums.AgentLifecycleState;
import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AbstractTeamAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.AgentMessage.MessageType;
import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.infrastructure.converter.AgentMessageConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 基于 Spring AI ChatClient 的 ReAct Agent 运行时。
 */
@RequiredArgsConstructor
public class SpringReActAgent extends AbstractReActAgent {

    private final ReActAgentContext context;

    private final ChatClient chatClient;

    private final TeamAgentFactory teamAgentFactory;

    private final AtomicReference<AgentLifecycleState> state = new AtomicReference<>(AgentLifecycleState.CREATED);

    @Override
    public void init() {
        state.set(AgentLifecycleState.RUNNING);
    }

    @Override
    public String getName() {
        return context.getAgent().getName();
    }

    @Override
    public Object getNativeAgent() {
        return chatClient;
    }

    @Override
    public AgentLifecycleState getState() {
        return state.get();
    }

    @Override
    public ReActAgentContext getContext() {
        return context;
    }

    @Override
    public Flux<AgentMessage> streamMessages(List<AgentMessage> messages) {
        beforeInference(messages);
        state.set(AgentLifecycleState.RUNNING);
        return streamInternal(messages)
                .doFinally(signal -> state.set(AgentLifecycleState.CREATED));
    }

    @Override
    public AgentMessage call(List<AgentMessage> messages) {
        beforeInference(messages);
        state.set(AgentLifecycleState.RUNNING);
        try {
            return afterInference(messages, callInternal(messages));
        } finally {
            state.set(AgentLifecycleState.CREATED);
        }
    }

    private Flux<AgentMessage> streamInternal(List<AgentMessage> messages) {
        return chatClient.prompt()
                .system(context.getSystemPrompt())
                .messages(toSpringMessages(messages))
                .stream()
                .chatResponse()
                .map(this::toAgentMessage);
    }

    private AgentMessage callInternal(List<AgentMessage> messages) {
        ChatResponse response = chatClient.prompt()
                .system(context.getSystemPrompt())
                .messages(toSpringMessages(messages))
                .call()
                .chatResponse();
        return afterInference(messages, toAgentMessage(response));
    }

    @Override
    public void interrupt() {
        state.set(AgentLifecycleState.STOPPING);
        state.set(AgentLifecycleState.STOPPED);
    }

    @Override
    public List<AbstractTeamAgent> teams() {
        return List.of();
    }

    @Override
    public void createTeam(AgentTeamType agentTeamType, ReActAgentContext leader, ReActAgentContext... followers) {
        teamAgentFactory.create(agentTeamType, leader, followers);
    }

    private List<Message> toSpringMessages(List<AgentMessage> messages) {
        return messages.stream().map(AgentMessageConverter::toMessage).toList();
    }

    private AgentMessage toAgentMessage(ChatResponse response) {
        AgentMessage agentMessage = new AgentMessage();
        agentMessage.setMessageType(MessageType.ASSISTANT);
        if (response.getResult() != null && response.getResult().getOutput() != null) {
            agentMessage.setText(response.getResult().getOutput().getText());
        }
        return agentMessage;
    }
}
