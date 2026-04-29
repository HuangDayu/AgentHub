package com.agenthub.api.dto;

import java.time.Instant;

public record AgentTeamResponse(
        String id,
        String tenantId,
        String workspaceId,
        String teamCode,
        String name,
        String description,
        String coordinationMode,
        String memberConfig,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
