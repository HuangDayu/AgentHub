package com.agenthub.api.dto;

public record CreateAgentTeamRequest(
        String tenantId,
        String workspaceId,
        String teamCode,
        String name,
        String description,
        String coordinationMode,
        String memberConfig
) {
}
