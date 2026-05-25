package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * Span Entity.
 * 数据库持久化对象.
 */
@Data
@TableName("spans")
public class SpanEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String spanId;
    private String traceId;
    private String parentSpanId;
    private String name;
    private String kind;

    private String startTimeUnixNano;
    private String endTimeUnixNano;
    private Long latencyNs;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String attributes;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String events;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String links;

    private Integer statusCode;
    private String statusMessage;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String resource;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String scope;

    private String model;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private String conversationId;

    private String runId;
    private String agentId;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
