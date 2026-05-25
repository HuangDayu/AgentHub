package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * Metric Entity.
 */
@Data
@TableName("metrics")
public class MetricEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String metricType;
    private String metricName;

    private Double metricValue;

    private String runId;
    private String agentId;
    private String traceId;
    private String spanId;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String labels;

    private Instant timestamp;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
