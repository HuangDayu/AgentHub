package com.agenthub.api.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.domain.model.Workspace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 工作空间响应DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    /**
     * 工作空间ID
     */
    private String id;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 工作空间编码
     */
    private String workspaceCode;
    /**
     * 工作空间名称
     */
    private String name;
    /**
     * 区域
     */
    private String region;
    /**
     * 工作空间状态
     */
    private Workspace.WorkspaceStatus status;
    /**
     * 创建时间
     */
    private Instant createdAt;
    /**
     * 更新时间
     */
    private Instant updatedAt;

    public static WorkspaceResponse from(Workspace workspace) {
        return BeanUtil.copyProperties(workspace, WorkspaceResponse.class);
    }
}
