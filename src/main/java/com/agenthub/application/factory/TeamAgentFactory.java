package com.agenthub.application.factory;

import com.agenthub.domain.enums.AgentTeamType;
import com.agenthub.domain.model.AbstractTeamAgent;
import com.agenthub.domain.model.ReActAgentContext;

/**
 * @author huangdayu
 */
public interface TeamAgentFactory {

    AbstractTeamAgent create(AgentTeamType agentTeamType, ReActAgentContext leader, ReActAgentContext... followers);

}
