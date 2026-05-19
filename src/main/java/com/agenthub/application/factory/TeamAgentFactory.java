package com.agenthub.application.factory;

import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.agent.AbstractTeamAgent;
import com.agenthub.domain.model.agent.ReActAgentContext;

/**
 * @author huangdayu
 */
public interface TeamAgentFactory {

    AbstractTeamAgent create(AgentTeamType agentTeamType, ReActAgentContext leader, ReActAgentContext... followers);

}
