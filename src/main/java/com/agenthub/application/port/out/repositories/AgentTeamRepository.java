package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.AgentTeam;

import java.util.List;
import java.util.Optional;

/**
 * Agent团队仓储接口，定义团队的持久化操作。
 */
public interface AgentTeamRepository {

    AgentTeam save(AgentTeam team);

    Optional<AgentTeam> findById(String teamId);

    List<AgentTeam> findAll();

    List<AgentTeam> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    void deleteById(String teamId);
}
