package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduledTaskRequest {
    private String tenantId;
    private String workspaceId;
    private String taskCode;
    private String name;
    private String description;
    private String taskType;
    private String cronExpression;
    private String executorConfig;
    private String prompt;
}
