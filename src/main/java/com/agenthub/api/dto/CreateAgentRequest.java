package com.agenthub.api.dto;

public record CreateAgentRequest(
        String tenantId,
        String workspaceId,
        String agentCode,
        String name,
        String description
) {
}
