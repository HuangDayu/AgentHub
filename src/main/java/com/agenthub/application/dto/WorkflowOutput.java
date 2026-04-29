package com.agenthub.application.dto;

import java.time.Instant;

public record WorkflowOutput(
        String id,
        String tenantId,
        String workspaceId,
        String workflowCode,
        String name,
        String description,
        String graphDefinition,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
