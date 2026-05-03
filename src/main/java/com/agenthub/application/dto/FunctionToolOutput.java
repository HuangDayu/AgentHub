package com.agenthub.application.dto;

import java.time.Instant;

public record FunctionToolOutput(
    String id,
    String tenantId,
    String toolClassName,
    String toolName,
    String description,
    String category,
    int methodCount,
    boolean enabled,
    boolean systemTool,
    Instant createdAt,
    Instant updatedAt
) {}
