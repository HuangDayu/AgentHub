package com.agenthub.api.dto;

public record CreateSecurityPolicyRequest(
        String tenantId,
        String workspaceId,
        String name,
        String description
) {
}
