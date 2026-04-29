package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.Workspace;

import java.util.List;
import java.util.Optional;

/**
 * 工作空间仓储端口.
 * <p>
 * 定义工作空间持久化的领域接口。
 * </p>
 */
public interface WorkspaceRepository {

    /**
     * 保存工作空间。
     *
     * @param workspace 待保存的工作空间
     * @return 保存后的工作空间
     */
    Workspace save(Workspace workspace);

    /**
     * 根据工作空间ID查找工作空间。
     *
     * @param workspaceId 工作空间ID
     * @return 包含工作空间的Optional，不存在时为空
     */
    Optional<Workspace> findById(String workspaceId);

    /**
     * 分页查询指定租户下的工作空间列表。
     *
     * @param page     页码（从0开始）
     * @param size     每页大小
     * @return 工作空间列表
     */
    List<Workspace> findByTenantId(int page, int size);

    /**
     * 统计指定租户下的工作空间数量。
     *
     * @param tenantId 租户ID
     * @return 工作空间数量
     */
    long countByTenantId(String tenantId);

    /**
     * 根据租户ID分页查找工作空间。
     */
    List<Workspace> findWorkspacesByTenantId(String tenantId, int page, int size);

}
