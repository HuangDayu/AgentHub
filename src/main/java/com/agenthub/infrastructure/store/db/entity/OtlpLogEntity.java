package com.agenthub.infrastructure.store.db.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * OTLP Log数据实体类，用于存储OpenTelemetry日志数据。
 */
@Data
@TableName("otlp_log")
public class OtlpLogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 日志ID
     */
    private String logId;
    
    /**
     * 追踪ID（可选，关联到Span）
     */
    private String traceId;
    
    /**
     * Span ID（可选，关联到Span）
     */
    private String spanId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 日志级别：TRACE, DEBUG, INFO, WARN, ERROR, FATAL
     */
    private String severity;
    
    /**
     * 日志级别数值
     */
    private Integer severityNumber;
    
    /**
     * 日志内容
     */
    private String body;
    
    /**
     * 属性JSON字符串
     */
    private String attributes;
    
    /**
     * 时间戳（纳秒）
     */
    private Long timestamp;
    
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
