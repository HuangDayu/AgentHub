package com.agenthub.api.dto;

import lombok.Data;
import java.time.Instant;

/**
 * OTLP Log响应DTO
 */
@Data
public class OtlpLogResponse {
    private String id;
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
