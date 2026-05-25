package com.agenthub.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Span Response DTO.
 */
@Data
public class SpanResponse {
    private String id;
    private String spanId;
    private String traceId;
    private String parentSpanId;
    private String name;
    private String kind;

    private String startTimeUnixNano;
    private String endTimeUnixNano;
    private Long latencyNs;

    private Map<String, Object> attributes;
    private List<Map<String, Object>> events;

    private Integer statusCode;
    private String statusMessage;

    private String model;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;

    private String runId;
    private String agentId;

    private Instant createdAt;
}
