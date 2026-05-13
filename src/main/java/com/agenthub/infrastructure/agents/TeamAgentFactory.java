package com.agenthub.infrastructure.agents;

import com.agenthub.domain.model.MultiAgentTeamType;
import com.agenthub.domain.model.ReActAgentContext;

/**
 * @author huangdayu
 */
public interface TeamAgentFactory {

    AbstractTeamAgent create(MultiAgentTeamType agentTeamType, ReActAgentContext leader, ReActAgentContext... followers);

}
