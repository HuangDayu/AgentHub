package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("app.guardrail_policy")
public class GuardrailStrategyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id",fill = FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String description;
    private Boolean inputValidationEnabled;
    private Boolean outputValidationEnabled;
    private Boolean piiDetectionEnabled;
    private Boolean piiMaskingEnabled;
    private Boolean promptInjectionDetection;
    private Integer maxInputLength;
    private Integer maxOutputLength;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;
}
