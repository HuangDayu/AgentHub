package com.agenthub.api.dto;

public record CreateWorkflowRequest(
        String tenantId,
        String workspaceId,
        String workflowCode,
        String name,
        String description,
        String graphDefinition
) {
}
