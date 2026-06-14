package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.strategy.PermissionStrategy;

import java.util.List;
import java.util.Optional;

/**
 * 权限策略仓储端口
 */
public interface PermissionStrategyRepository {
    PermissionStrategy save(PermissionStrategy policy);
    Optional<PermissionStrategy> findById(String id);
    List<PermissionStrategy> findByWorkspaceId(String workspaceId);
    void deleteById(String id);
    boolean existsByWorkspaceIdAndName(String workspaceId, String name);
}
