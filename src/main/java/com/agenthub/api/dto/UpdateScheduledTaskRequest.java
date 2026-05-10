package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduledTaskRequest {
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String cronExpression;
    private String executorConfig;
    private String prompt;
}
