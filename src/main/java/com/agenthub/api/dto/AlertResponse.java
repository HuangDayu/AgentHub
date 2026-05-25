package com.agenthub.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Alert Response DTO.
 */
@Data
public class AlertResponse {
    private String id;
    private String alertLevel;
    private String alertType;

    private String title;
    private String message;

    private String runId;
    private String agentId;
    private String traceId;

    private Map<String, Object> metadata;

    private boolean resolved;
    private Instant resolvedAt;
    private String resolvedBy;

    private Instant createdAt;
}
