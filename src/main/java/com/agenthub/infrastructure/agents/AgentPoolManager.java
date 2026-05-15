package com.agenthub.infrastructure.agents;

import com.agenthub.domain.event.AgentConfigDeletedEvent;
import com.agenthub.domain.event.AgentConfigUpdatedEvent;
import com.agenthub.domain.model.Agent;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantThreadContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentPoolManager {

    private static final Map<String, AbstractReActAgent> AGENT_POOL = new ConcurrentHashMap<>();
    private static final Map<String, ReActAgentContext> CONTEXT_POOL = new ConcurrentHashMap<>();
    private final ReActAgentFactory agentFactory;
    private final AgentContextManager agentContextManager;
    private static final Set<String> agentIds = new HashSet<>();

    public AbstractReActAgent getAgent(Agent agent) {
        return AGENT_POOL.computeIfAbsent(agent.getId(), id -> agentFactory.create(getReActAgentContext(agent.getId())));
    }

    public AbstractReActAgent getAgent(String agentId) {
        return AGENT_POOL.computeIfAbsent(agentId, id -> agentFactory.create(getReActAgentContext(agentId)));
    }

    private ReActAgentContext getReActAgentContext(String agentId) {
        return CONTEXT_POOL.computeIfAbsent(agentId, id -> agentContextManager.buildContext(agentId));
    }

    private AbstractReActAgent reloadAgent(String agentId) {
        if (AGENT_POOL.containsKey(agentId)) {
            AGENT_POOL.remove(agentId);
            CONTEXT_POOL.remove(agentId);
            AbstractReActAgent agent = getAgent(agentId);
            agent.init();
            return agent;
        }
        return null;
    }

    @Async("ttlExecutorService")
    @EventListener
    public void handleConfigDeletedEvent(AgentConfigDeletedEvent event) {
        agentIds.addAll(event.getConfigs().stream().map(AgentConfig::getAgentId).collect(Collectors.toSet()));
    }

    @Async("ttlExecutorService")
    @EventListener
    public void handleConfigUpdatedEvent(AgentConfigUpdatedEvent event) {
        agentIds.addAll(event.getConfigs().stream().map(AgentConfig::getAgentId).collect(Collectors.toSet()));
    }


    @Scheduled(fixedRate = 60000)
    public void executeTask() {
        if (!agentIds.isEmpty()) {
            agentIds.forEach(v -> {
                try {
                    TenantContextHolder.open(new TenantThreadContext(null, null, null, true));
                    reloadAgent(v);
                    agentIds.remove(v);
                } catch (Exception e) {
                    log.error("Failed to reload agent: {}", v, e);
                }
            });
        }
    }
}
