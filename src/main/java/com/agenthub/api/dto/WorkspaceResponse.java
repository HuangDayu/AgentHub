package com.agenthub.api.dto;

import com.agenthub.domain.model.Workspace;

import java.time.Instant;

/**
 * 工作空间响应DTO.
 */
public record WorkspaceResponse(
        /** 工作空间ID */String id,
        /** 租户ID */String tenantId,
        /** 工作空间编码 */String workspaceCode,
        /** 工作空间名称 */String name,
        /** 区域 */String region,
        /** 工作空间状态 */String status,
        /** 创建时间 */Instant createdAt,
        /** 更新时间 */Instant updatedAt
) {
    /**
     * 从工作空间领域对象转换为响应DTO。
     *
     * @param workspace 工作空间领域对象
     * @return 工作空间响应DTO
     */
    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.id(), workspace.tenantId(), workspace.workspaceCode(),
                workspace.name(), workspace.region(), workspace.status().name(),
                workspace.createdAt(), workspace.updatedAt()
        );
    }
}
