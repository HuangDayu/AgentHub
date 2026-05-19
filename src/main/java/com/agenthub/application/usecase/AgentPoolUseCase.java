package com.agenthub.application.usecase;

import cn.hutool.core.map.multi.RowKeyTable;
import cn.hutool.core.map.multi.Table;
import com.agenthub.application.factory.AgentContextFactory;
import com.agenthub.application.factory.AgentPoolFactory;
import com.agenthub.application.factory.ReActAgentFactory;
import com.agenthub.common.annotations.IgnoreTenantContext;
import com.agenthub.domain.event.AgentConfigDeletedEvent;
import com.agenthub.domain.event.AgentConfigUpdatedEvent;
import com.agenthub.domain.model.AbstractReActAgent;
import com.agenthub.domain.model.Agent;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.ReActAgentContext;
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
public class AgentPoolUseCase implements AgentPoolFactory {

    private static final Table<String, String, AbstractReActAgent> AGENT_POOL = new RowKeyTable<>(new ConcurrentHashMap<>(), ConcurrentHashMap::new);
    private static final Table<String, String, ReActAgentContext> CONTEXT_POOL = new RowKeyTable<>(new ConcurrentHashMap<>(), ConcurrentHashMap::new);
    private final ReActAgentFactory agentFactory;
    private final AgentContextFactory agentContextFactory;
    private static final Set<String> agentIds = new HashSet<>();

    @Override
    public AbstractReActAgent getAgent(Agent agent, String sessionId) {
        return getAgent(agent.getId(), sessionId);
    }

    @Override
    public AbstractReActAgent getAgent(String agentId, String sessionId) {
        AbstractReActAgent agent = AGENT_POOL.get(sessionId, agentId);
        if (agent == null) {
            agent = agentFactory.create(getReActAgentContext(agentId, sessionId));
            AGENT_POOL.put(sessionId, agentId, agent);
        }
        return agent;
    }

    private ReActAgentContext getReActAgentContext(String agentId, String sessionId) {
        ReActAgentContext context = CONTEXT_POOL.get(sessionId, agentId);
        if (context == null) {
            context = agentContextFactory.buildContext(agentId, sessionId);
            CONTEXT_POOL.put(sessionId, agentId, context);
        }
        return context;
    }

    private void reloadAgent(String agentId) {
        for (Map.Entry<String, AbstractReActAgent> entry : AGENT_POOL.getColumn(agentId).entrySet()) {
            AGENT_POOL.remove(entry.getKey(), agentId);
            CONTEXT_POOL.remove(entry.getKey(), agentId);
            AbstractReActAgent agent = getAgent(entry.getKey(), agentId);
            agent.init();
        }
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
    @IgnoreTenantContext
    public void executeTask() {
        if (!agentIds.isEmpty()) {
            agentIds.forEach(v -> {
                try {
                    reloadAgent(v);
                    agentIds.remove(v);
                } catch (Exception e) {
                    log.error("Failed to reload agent: {}", v, e);
                }
            });
        }
    }
}
