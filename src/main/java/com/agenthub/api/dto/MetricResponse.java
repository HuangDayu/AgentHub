package com.agenthub.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Metric Response DTO.
 */
@Data
public class MetricResponse {
    private String id;
    private String metricType;
    private String metricName;
    private Double metricValue;

    private String runId;
    private String agentId;
    private String traceId;
    private String spanId;

    private Map<String, Object> labels;

    private Instant timestamp;
    private Instant createdAt;
}
