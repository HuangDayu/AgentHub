package com.agenthub.infrastructure.store.db.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * OTLP Span数据实体类，用于存储OpenTelemetry追踪数据。
 */
@Data
@TableName("otlp_span")
public class OtlpSpanEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * Span的唯一标识符
     */
    private String spanId;
    
    /**
     * 追踪ID
     */
    private String traceId;
    
    /**
     * 父Span ID
     */
    private String parentSpanId;
    
    /**
     * 操作名称
     */
    private String operationName;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * Span类型：INTERNAL, SERVER, CLIENT, PRODUCER, CONSUMER
     */
    private String kind;
    
    /**
     * 开始时间戳（纳秒）
     */
    private Long startTimestamp;
    
    /**
     * 结束时间戳（纳秒）
     */
    private Long endTimestamp;
    
    /**
     * 持续时间（纳秒）
     */
    private Long duration;
    
    /**
     * Span状态：UNSET, OK, ERROR
     */
    private String status;
    
    /**
     * 状态描述信息
     */
    private String statusDescription;
    
    /**
     * 属性JSON字符串
     */
    private String attributes;
    
    /**
     * 事件JSON字符串
     */
    private String events;
    
    /**
     * 链接JSON字符串
     */
    private String links;
    
    /**
     * 租户ID
     */
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    
    /**
     * 工作空间ID
     */
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
