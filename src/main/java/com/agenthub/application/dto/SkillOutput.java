package com.agenthub.application.dto;

import java.time.Instant;

public record SkillOutput(
        String id,
        String tenantId,
        String workspaceId,
        String skillCode,
        String name,
        String description,
        String skillType,
        String skillPath,
        String skillFilesTree,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
