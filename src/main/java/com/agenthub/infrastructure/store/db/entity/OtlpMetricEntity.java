package com.agenthub.infrastructure.store.db.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * OTLP Metric数据实体类，用于存储OpenTelemetry指标数据。
 */
@Data
@TableName("otlp_metric")
public class OtlpMetricEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 指标名称
     */
    private String metricName;
    
    /**
     * 指标描述
     */
    private String description;
    
    /**
     * 指标单位
     */
    private String unit;
    
    /**
     * 指标类型：GAUGE, COUNTER, HISTOGRAM, SUMMARY
     */
    private String metricType;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 指标值（JSON格式，支持多种类型）
     */
    private String value;
    
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
