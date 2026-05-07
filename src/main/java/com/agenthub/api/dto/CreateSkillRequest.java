package com.agenthub.api.dto;

public record CreateSkillRequest(
        String tenantId,
        String workspaceId,
        String skillCode,
        String name,
        String description,
        String skillType,
        String skillPath,
        String skillFilesTree
) {
}
