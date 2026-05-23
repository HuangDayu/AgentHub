package com.agenthub.domain.model.telemetry;

import lombok.Data;
import java.time.Instant;

/**
 * OTLP Log领域模型
 */
@Data
public class OtlpLog {
    private String logId;
    private String traceId;
    private String spanId;
    private String serviceName;
    private String severity;
    private Integer severityNumber;
    private String body;
    private String attributes;
    private Long timestamp;
    private Instant createdAt;
}
