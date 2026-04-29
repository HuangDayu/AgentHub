package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.AgentConfig;

import java.util.List;
import java.util.Optional;

public interface AgentConfigRepository {
    AgentConfig save(AgentConfig config);

    Optional<AgentConfig> findById(String id);

    List<AgentConfig> findByAgentId(String agentId);

    List<AgentConfig> findByAgentIdAndCategory(String agentId, AgentConfig.Category category);

    List<AgentConfig> findEnabledAgentConfigs(String agentId, AgentConfig.Category category, AgentConfig.Type type);

    AgentConfig findOneAgentConfig(String agentId, AgentConfig.Category category, AgentConfig.Type type);

    void deleteById(String id);

    void deleteByAgentId(String agentId);

    AgentConfig update(AgentConfig config);
}
