package com.agenthub.application.dto;

import java.time.Instant;

public record AgentTeamOutput(
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
