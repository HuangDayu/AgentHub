package com.agenthub.infrastructure.agents;

import com.agenthub.domain.model.AgentLifecycleState;
import com.agenthub.domain.model.ReActAgentContext;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author huangdayu
 */
public abstract class AbstractReActAgent {
    public abstract void init();

    public abstract String getName();

    public abstract Object getNativeAgent();

    public abstract AgentLifecycleState getState();

    public abstract ReActAgentContext getContext();

    public abstract Flux<Message> streamMessages(String sessionId, String userMessage);

    public abstract AssistantMessage call(String sessionId, String userMessage);

    public abstract void interrupt();

    public abstract List<AbstractTeamAgent> teams();

    public abstract void addTeam(AbstractTeamAgent teamAgent);

}
