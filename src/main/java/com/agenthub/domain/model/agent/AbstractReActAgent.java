package com.agenthub.domain.model.agent;

import com.agenthub.domain.enums.AgentLifecycleState;
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

    public abstract Flux<AgentMessage> streamMessages(String sessionId, String userMessage);

    public abstract AgentMessage call(String sessionId, String userMessage);

    public abstract void interrupt();

    public abstract List<AbstractTeamAgent> teams();

    public abstract void addTeam(AbstractTeamAgent teamAgent);

}
