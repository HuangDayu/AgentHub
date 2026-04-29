package com.agenthub.api.dto;

import java.time.Instant;

public record SecurityPolicyResponse(
        String id,
        String tenantId,
        String workspaceId,
        String name,
        String description,
        boolean inputValidation,
        boolean outputFiltering,
        boolean rateLimitEnabled,
        int rateLimitPerMinute,
        boolean contentModeration,
        boolean piiDetection,
        String allowedDomains,
        String blockedPatterns,
        Instant createdAt,
        Instant updatedAt
) {
}
