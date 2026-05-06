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
    
    private final ReActAgentManager reActAgentManager;
    
    @Override
    public String getName(String agentId) {
        AbstractReActAgent agent = getAgent(agentId);
        return agent != null ? agent.getName() : null;
    }
    
    @Override
    public Flux<Message> streamMessages(String agentId, String userMessage) {
        AbstractReActAgent agent = getAgent(agentId);
        if (agent == null) return Flux.empty();
        return agent.streamMessages(userMessage);
    }
    
    @Override
    public AssistantMessage call(String agentId, String userMessage) {
        AbstractReActAgent agent = getAgent(agentId);
        if (agent == null) return null;
        return agent.call(userMessage);
    }
    
    @Override
    public void interrupt(String agentId) {
        AbstractReActAgent agent = getAgent(agentId);
        if (agent != null) agent.interrupt();
    }
    
    private AbstractReActAgent getAgent(String agentId) {
        return reActAgentManager.getAgent(agentId);
    }
}
