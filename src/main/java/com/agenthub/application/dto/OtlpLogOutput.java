package com.agenthub.application.dto;

import lombok.Data;
import java.time.Instant;

/**
 * OTLP Log输出DTO
 */
@Data
public class OtlpLogOutput {
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
