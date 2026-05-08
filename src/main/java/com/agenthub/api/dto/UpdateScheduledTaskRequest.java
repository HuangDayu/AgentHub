package com.agenthub.api.dto;

public record UpdateScheduledTaskRequest(
    String tenantId,
    String workspaceId,
    String name,
    String description,
    String cronExpression,
    String executorConfig,
    String prompt
) {}
