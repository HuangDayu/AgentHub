package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.Agent;

import java.util.List;
import java.util.Optional;

/**
 * 智能体仓储接口，定义智能体的持久化操作。
 */
public interface AgentRepository {

    Agent save(Agent agent);

    Optional<Agent> findById(String agentId);

    List<Agent> findAll();

    void deleteById(String agentId);

    List<Agent> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);
}
