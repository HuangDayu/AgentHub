package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * Trace Entity.
 * 数据库持久化对象.
 */
@Data
@TableName("traces")
public class TraceEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String traceId;
    private String runId;

    private String rootSpanId;
    private Integer spanCount;

    private String startTimeUnixNano;
    private String endTimeUnixNano;
    private Long durationNs;

    private Integer statusCode;
    private String errorMessage;

    private Long totalTokens;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
