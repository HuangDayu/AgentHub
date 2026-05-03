package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("app.security_policy")
public class SecurityPolicyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String description;
    @TableField(value = "input_validation")
    private boolean inputValidation;
    @TableField(value = "output_filtering")
    private boolean outputFiltering;
    @TableField(value = "rate_limit_enabled")
    private boolean rateLimitEnabled;
    @TableField(value = "rate_limit_per_minute")
    private int rateLimitPerMinute;
    @TableField(value = "content_moderation")
    private boolean contentModeration;
    @TableField(value = "pii_detection")
    private boolean piiDetection;
    @TableField(value = "allowed_domains")
    private String allowedDomains;
    @TableField(value = "blocked_patterns")
    private String blockedPatterns;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;


}
