package com.agenthub.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * 创建 Metric 请求 DTO.
 */
@Data
public class CreateMetricRequest {
    private String metricType;
    private String metricName;
    private Double metricValue;

    private String runId;
    private String agentId;
    private String traceId;
    private String spanId;

    private Map<String, Object> labels;
}
