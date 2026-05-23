package com.agenthub.application.dto;

import lombok.Data;
import java.time.Instant;

/**
 * OTLP Span输出DTO
 */
@Data
public class OtlpSpanOutput {
    private String spanId;
    private String traceId;
    private String parentSpanId;
    private String operationName;
    private String serviceName;
    private String kind;
    private Long startTimestamp;
    private Long endTimestamp;
    private Long duration;
    private String status;
    private String statusDescription;
    private String attributes;
    private String events;
    private String links;
    private Instant createdAt;
}
