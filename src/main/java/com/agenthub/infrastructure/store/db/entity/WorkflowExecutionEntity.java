package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

/**
 * 工作流执行记录数据库实体.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("workflow_execution")
public class WorkflowExecutionEntity {

    /**
     * 主键ID.
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 工作流ID.
     */
    @TableField(value = "workflow_id")
    private String workflowId;

    /**
     * 租户ID.
     */
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    /**
     * 工作空间ID.
     */
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    /**
     * 执行ID.
     */
    @TableField(value = "execution_id")
    private String executionId;

    /**
     * 执行状态.
     */
    private String status;

    /**
     * 输入参数.
     */
    private String input;

    /**
     * 输出结果.
     */
    private String output;

    /**
     * 错误信息.
     */
    @TableField(value = "error_info")
    private String errorInfo;

    /**
     * 开始时间.
     */
    @TableField(value = "start_time")
    private Instant startTime;

    /**
     * 结束时间.
     */
    @TableField(value = "end_time")
    private Instant endTime;

    /**
     * 执行时长（毫秒）.
     */
    private Long duration;

    /**
     * 创建时间.
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    /**
     * 更新时间.
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
