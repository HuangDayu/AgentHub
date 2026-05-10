package com.agenthub.api.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTaskResponse {
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
}
