package com.agenthub.application.dto;

import java.time.Instant;

public record MemoryOutput(
        String id,
        String tenantId,
        String workspaceId,
        String agentId,
        String memoryType,
        String content,
        String metadata,
        double importance,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt
) {
}
