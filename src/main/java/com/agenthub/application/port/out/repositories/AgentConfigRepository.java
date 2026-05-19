package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.agent.AgentConfig;
import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;

import java.util.List;
import java.util.Optional;

public interface AgentConfigRepository {
    AgentConfig saveOrUpdate(AgentConfig config);

    Optional<AgentConfig> findById(String id);

    List<AgentConfig> findByAgentId(String agentId);

    List<AgentConfig> findByAgentIdAndEnabled(String agentId);

    List<AgentConfig> findByAgentIdAndCategory(String agentId, AgentConfigCategory category);

    List<AgentConfig> findEnabledAgentConfigs(String agentId, AgentConfigCategory category, AgentConfigType type);

    AgentConfig findOneAgentConfig(String agentId, AgentConfigCategory category, AgentConfigType type);

    List<AgentConfig> findAgentConfigs(AgentConfigCategory category, AgentConfigType type, List<String> configIds);

    void deleteById(String id);

    void deleteByAgentId(String agentId);

    AgentConfig update(AgentConfig config);

    String getConfigId(String agentId, AgentConfigCategory category, AgentConfigType type);

    void deleteByIds(List<String> ids);

}
