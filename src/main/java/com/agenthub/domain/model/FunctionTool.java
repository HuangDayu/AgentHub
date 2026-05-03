package com.agenthub.domain.model;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * Function Tool 领域模型
 */
public class FunctionTool {
    private String id;
    private String tenantId;
    private String toolClassName;
    private String toolName;
    private String description;
    private String category;
    private int methodCount;
    private boolean enabled;
    private boolean systemTool;
    private Instant createdAt;
    private Instant updatedAt;

    public FunctionTool() {
        this.id = randomId();
        this.enabled = true;
        this.systemTool = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static FunctionTool create(String toolClassName, String toolName, 
                                      String description, String category, int methodCount) {
        FunctionTool tool = new FunctionTool();
        tool.toolClassName = toolClassName;
        tool.toolName = toolName;
        tool.description = description;
        tool.category = category;
        tool.methodCount = methodCount;
        return tool;
    }

    public FunctionTool enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
        return this;
    }

    public FunctionTool disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
        return this;
    }

    public FunctionTool update(String toolName, String description, String category, int methodCount) {
        this.toolName = toolName;
        this.description = description;
        this.category = category;
        this.methodCount = methodCount;
        this.updatedAt = Instant.now();
        return this;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getToolClassName() { return toolClassName; }
    public void setToolClassName(String toolClassName) { this.toolClassName = toolClassName; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getMethodCount() { return methodCount; }
    public void setMethodCount(int methodCount) { this.methodCount = methodCount; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isSystemTool() { return systemTool; }
    public void setSystemTool(boolean systemTool) { this.systemTool = systemTool; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
