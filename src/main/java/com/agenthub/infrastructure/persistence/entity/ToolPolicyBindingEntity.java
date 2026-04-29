package com.agenthub.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@TableName("app.tool_policy_binding")
public class ToolPolicyBindingEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String toolPolicyId;
    private String toolId;
    private Integer priority;
    private Boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getToolPolicyId() { return toolPolicyId; }
    public void setToolPolicyId(String toolPolicyId) { this.toolPolicyId = toolPolicyId; }
    public String getToolId() { return toolId; }
    public void setToolId(String toolId) { this.toolId = toolId; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
