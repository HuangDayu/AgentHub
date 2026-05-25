package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * Alert Entity.
 */
@Data
@TableName("alerts")
public class AlertEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String alertLevel;
    private String alertType;

    private String title;
    private String message;

    private String runId;
    private String agentId;
    private String traceId;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String metadata;

    private Boolean resolved;
    private Instant resolvedAt;
    private String resolvedBy;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
