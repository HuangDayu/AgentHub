package com.agenthub.api.dto;

import java.time.Instant;

public record CreateMemoryRequest(
        String tenantId,
        String workspaceId,
        String agentId,
        String memoryType,
        String content,
        String metadata,
        double importance,
        Instant expiresAt
) {
}
