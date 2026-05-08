package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.AgentConfigCategory;
import com.agenthub.domain.model.AgentConfigType;

import java.util.List;
import java.util.Optional;

public interface AgentConfigRepository {
    AgentConfig save(AgentConfig config);

    Optional<AgentConfig> findById(String id);

    List<AgentConfig> findByAgentId(String agentId);

    List<AgentConfig> findByAgentIdAndEnabled(String agentId);

    List<AgentConfig> findByAgentIdAndCategory(String agentId, AgentConfigCategory category);

    List<AgentConfig> findEnabledAgentConfigs(String agentId, AgentConfigCategory category, AgentConfigType type);

    AgentConfig findOneAgentConfig(String agentId, AgentConfigCategory category, AgentConfigType type);

    void deleteById(String id);

    void deleteByAgentId(String agentId);

    AgentConfig update(AgentConfig config);

    String getConfigId(String agentId, AgentConfigCategory category, AgentConfigType type);
}
