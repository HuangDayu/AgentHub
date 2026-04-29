package com.agenthub.api.dto;

import java.time.Instant;

public record SkillResponse(
        String id,
        String tenantId,
        String workspaceId,
        String skillCode,
        String name,
        String description,
        String skillType,
        String definition,
        String parameters,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
