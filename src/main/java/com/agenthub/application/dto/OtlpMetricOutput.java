package com.agenthub.application.dto;

import lombok.Data;
import java.time.Instant;

/**
 * OTLP Metric输出DTO
 */
@Data
public class OtlpMetricOutput {
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
