package com.agenthub.infrastructure.agents;

import com.agenthub.domain.model.Agent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AgentPoolManager {

    public static final Map<String, AbstractReActAgent> AGENT_POOL = new ConcurrentHashMap<>();
    private final ReActAgentFactory agentFactory;
    private final AgentContextManager agentContextManager;

    public AbstractReActAgent getAgent(Agent agent) {
        return AGENT_POOL.computeIfAbsent(agent.getId(), id -> agentFactory.create(agentContextManager.buildContext(agent.getId())));
    }

    public AbstractReActAgent getAgent(String agentId) {
        return AGENT_POOL.computeIfAbsent(agentId, id -> agentFactory.create(agentContextManager.buildContext(agentId)));
    }

    public void removeAgent(String agentId) {
        AGENT_POOL.remove(agentId);
    }

}
