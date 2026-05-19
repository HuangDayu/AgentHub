package com.agenthub.domain.model;

import com.agenthub.domain.enums.AgentTeamType;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author huangdayu
 */
public abstract class AbstractTeamAgent {

    public abstract String getName();

    public abstract AgentTeamType getAgentTeamType();

    public abstract ReActAgentContext getContext();

    public abstract Flux<AgentMessage> streamMessages(String userMessage);

    public abstract List<AbstractReActAgent> followers();

}
