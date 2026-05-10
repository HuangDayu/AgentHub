package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 工作空间领域模型.
 * <p>
 * 表示租户下的工作空间实体，包含工作空间的基本信息和业务操作方法。
 * 采用不可变设计，所有状态变更操作都返回新的实例。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceCommand {
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
    private WorkspaceStatus status;
    /**
     * 创建时间
     */
    private Instant createdAt;
    /**
     * 更新时间
     */
    private Instant updatedAt;


    /**
     * 工作空间状态枚举.
     */
    public enum WorkspaceStatus {ACTIVE, SUSPENDED}
}
