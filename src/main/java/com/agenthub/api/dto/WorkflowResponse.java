package com.agenthub.api.dto;

import java.time.Instant;

public record WorkflowResponse(
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
