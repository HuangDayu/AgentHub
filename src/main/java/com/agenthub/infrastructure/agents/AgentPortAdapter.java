package com.agenthub.infrastructure.agents;

import com.agenthub.application.port.out.agent.AgentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Agent端口适配器，将ReActAgentPooling适配为AgentPort。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AgentPortAdapter implements AgentPort {

    private final AgentPoolManager agentPoolManager;

    @Override
    public Flux<Message> streamMessages(String agentId, String sessionId, String userMessage) {
        AbstractReActAgent agent = getAgent(agentId, sessionId);
        if (agent == null) return Flux.empty();
        return agent.streamMessages(sessionId, userMessage);
    }

    @Override
    public AssistantMessage call(String agentId, String sessionId, String userMessage) {
        AbstractReActAgent agent = getAgent(agentId, sessionId);
        if (agent == null) return null;
        return agent.call(sessionId, userMessage);
    }

    @Override
    public void interrupt(String agentId, String sessionId) {
        AbstractReActAgent agent = getAgent(agentId, sessionId);
        if (agent != null) agent.interrupt();
    }

    private AbstractReActAgent getAgent(String agentId, String sessionId) {
        return agentPoolManager.getAgent(agentId, sessionId);
    }
}
