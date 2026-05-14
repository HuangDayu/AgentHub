package com.agenthub.infrastructure.agents.alibaba;

import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.infrastructure.agents.AgentPoolManager;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantThreadContext;
import com.alibaba.cloud.ai.agent.studio.loader.AgentLoader;
import com.alibaba.cloud.ai.graph.agent.Agent;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AliReActAgentLoader implements AgentLoader {

    private final AgentRepository agentRepository;
    private final AgentPoolManager agentPoolManager;

    @Override
    public @NonNull List<String> listAgents() {
        TenantContextHolder.open(new TenantThreadContext("100000002", "100000002", "100000002", false));
        return agentRepository.findAll().stream().map(com.agenthub.domain.model.Agent::getName).toList();
    }

    @Override
    public Agent loadAgent(String name) {
        TenantContextHolder.open(new TenantThreadContext("100000002", "100000002", "100000002", false));
        com.agenthub.domain.model.Agent agent = agentRepository.findByName(name);
        return (Agent) agentPoolManager.getAgent(agent).getNativeAgent();
    }
}
