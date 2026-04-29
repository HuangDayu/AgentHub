package com.agenthub.application.dto;

import java.time.Instant;

public record AgentConfigOutput(
        String id,
        String agentId,
        String category,
        String type,
        String configId,
        String description,
        int priority,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
