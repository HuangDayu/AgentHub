package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Metric 输出 DTO.
 */
@Data
@AllArgsConstructor
public class MetricOutput {
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
