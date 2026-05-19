package com.agenthub.application.factory;

import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.Agent;

/**
 * @author huangdayu
 */
public interface AgentPoolFactory {
    AbstractReActAgent getAgent(Agent agent, String sessionId);

    AbstractReActAgent getAgent(String agentId, String sessionId);
}
