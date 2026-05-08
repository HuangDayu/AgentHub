package com.agenthub.api.dto;

import java.time.LocalDateTime;

public record ScheduledTaskResponse(
    String id,
    String tenantId,
    String workspaceId,
    String taskCode,
    String name,
    String description,
    String taskType,
    String cronExpression,
    String executorConfig,
    String prompt,
    boolean enabled,
    LocalDateTime lastExecuteTime,
    LocalDateTime nextExecuteTime,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
