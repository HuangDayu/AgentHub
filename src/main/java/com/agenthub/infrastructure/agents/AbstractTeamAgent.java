package com.agenthub.infrastructure.agents;

import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.ReActAgentContext;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author huangdayu
 */
public abstract class AbstractTeamAgent {

    public abstract String getName();

    public abstract AgentTeamType getAgentTeamType();

    public abstract ReActAgentContext getContext();

    public abstract Flux<Message> streamMessages(String userMessage);

    public abstract List<AbstractReActAgent> followers();

}
