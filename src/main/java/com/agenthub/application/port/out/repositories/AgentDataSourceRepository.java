package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.datasource.AgentDataSource;

import java.util.List;
import java.util.Optional;

/**
 * Agent 数据源仓储端口
 */
public interface AgentDataSourceRepository {
    AgentDataSource save(AgentDataSource source);
    Optional<AgentDataSource> findById(String id);
    List<AgentDataSource> findByWorkspaceId(String workspaceId);
    List<AgentDataSource> findAll();
    void deleteById(String id);
    boolean existsByWorkspaceIdAndName(String workspaceId, String name);
}
