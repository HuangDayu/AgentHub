package com.agenthub.api.dto;

public record CreateScheduledTaskRequest(
    String tenantId,
    String workspaceId,
    String taskCode,
    String name,
    String description,
    String taskType,
    String cronExpression,
    String executorConfig,
    String prompt
) {}
