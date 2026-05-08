package com.agenthub.domain.model;

import java.time.LocalDateTime;

public class ScheduledTask {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String taskCode;
    private String name;
    private String description;
    private String taskType;
    private String cronExpression;
    private String executorConfig;
    private String prompt;
    private boolean enabled;
    private LocalDateTime lastExecuteTime;
    private LocalDateTime nextExecuteTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ScheduledTask() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public String getExecutorConfig() { return executorConfig; }
    public void setExecutorConfig(String executorConfig) { this.executorConfig = executorConfig; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getLastExecuteTime() { return lastExecuteTime; }
    public void setLastExecuteTime(LocalDateTime lastExecuteTime) { this.lastExecuteTime = lastExecuteTime; }

    public LocalDateTime getNextExecuteTime() { return nextExecuteTime; }
    public void setNextExecuteTime(LocalDateTime nextExecuteTime) { this.nextExecuteTime = nextExecuteTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
