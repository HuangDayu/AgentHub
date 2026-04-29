package com.agenthub.application.dto;

import java.time.Instant;

public record AgentOutput(
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
