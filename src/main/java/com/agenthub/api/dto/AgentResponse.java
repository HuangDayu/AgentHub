package com.agenthub.api.dto;

import java.time.Instant;

public record AgentResponse(
        String id,
        String tenantId,
        String workspaceId,
        String agentCode,
        String name,
        String description,
        String status,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
