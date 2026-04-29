package com.agenthub.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

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
    public Boolean getInputValidationEnabled() { return inputValidationEnabled; }
    public void setInputValidationEnabled(Boolean inputValidationEnabled) { this.inputValidationEnabled = inputValidationEnabled; }
    public Boolean getOutputValidationEnabled() { return outputValidationEnabled; }
    public void setOutputValidationEnabled(Boolean outputValidationEnabled) { this.outputValidationEnabled = outputValidationEnabled; }
    public Boolean getPiiDetectionEnabled() { return piiDetectionEnabled; }
    public void setPiiDetectionEnabled(Boolean piiDetectionEnabled) { this.piiDetectionEnabled = piiDetectionEnabled; }
    public Boolean getPiiMaskingEnabled() { return piiMaskingEnabled; }
    public void setPiiMaskingEnabled(Boolean piiMaskingEnabled) { this.piiMaskingEnabled = piiMaskingEnabled; }
    public Boolean getPromptInjectionDetection() { return promptInjectionDetection; }
    public void setPromptInjectionDetection(Boolean promptInjectionDetection) { this.promptInjectionDetection = promptInjectionDetection; }
    public Integer getMaxInputLength() { return maxInputLength; }
    public void setMaxInputLength(Integer maxInputLength) { this.maxInputLength = maxInputLength; }
    public Integer getMaxOutputLength() { return maxOutputLength; }
    public void setMaxOutputLength(Integer maxOutputLength) { this.maxOutputLength = maxOutputLength; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
