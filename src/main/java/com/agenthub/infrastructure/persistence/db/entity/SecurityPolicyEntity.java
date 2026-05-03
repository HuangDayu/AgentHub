package com.agenthub.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isInputValidation() { return inputValidation; }
    public void setInputValidation(boolean inputValidation) { this.inputValidation = inputValidation; }
    public boolean isOutputFiltering() { return outputFiltering; }
    public void setOutputFiltering(boolean outputFiltering) { this.outputFiltering = outputFiltering; }
    public boolean isRateLimitEnabled() { return rateLimitEnabled; }
    public void setRateLimitEnabled(boolean rateLimitEnabled) { this.rateLimitEnabled = rateLimitEnabled; }
    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    public boolean isContentModeration() { return contentModeration; }
    public void setContentModeration(boolean contentModeration) { this.contentModeration = contentModeration; }
    public boolean isPiiDetection() { return piiDetection; }
    public void setPiiDetection(boolean piiDetection) { this.piiDetection = piiDetection; }
    public String getAllowedDomains() { return allowedDomains; }
    public void setAllowedDomains(String allowedDomains) { this.allowedDomains = allowedDomains; }
    public String getBlockedPatterns() { return blockedPatterns; }
    public void setBlockedPatterns(String blockedPatterns) { this.blockedPatterns = blockedPatterns; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
