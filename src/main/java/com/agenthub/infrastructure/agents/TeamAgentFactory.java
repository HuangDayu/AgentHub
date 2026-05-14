package com.agenthub.infrastructure.agents;

import com.agenthub.domain.model.AgentTeamType;
import com.agenthub.domain.model.ReActAgentContext;

/**
 * @author huangdayu
 */
public interface TeamAgentFactory {

    AbstractTeamAgent create(AgentTeamType agentTeamType, ReActAgentContext leader, ReActAgentContext... followers);

}
