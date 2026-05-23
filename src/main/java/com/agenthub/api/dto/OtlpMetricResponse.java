package com.agenthub.api.dto;

import lombok.Data;
import java.time.Instant;

/**
 * OTLP Metric响应DTO
 */
@Data
public class OtlpMetricResponse {
    private String id;
    private String metricName;
    private String description;
    private String unit;
    private String metricType;
    private String serviceName;
    private String value;
    private String attributes;
    private Long timestamp;
    private Instant createdAt;
}
