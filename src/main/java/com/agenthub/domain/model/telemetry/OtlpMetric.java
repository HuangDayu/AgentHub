package com.agenthub.domain.model.telemetry;

import lombok.Data;
import java.time.Instant;

/**
 * OTLP Metric领域模型
 */
@Data
public class OtlpMetric {
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
