package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Alert 输出 DTO.
 */
@Data
@AllArgsConstructor
public class AlertOutput {
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
