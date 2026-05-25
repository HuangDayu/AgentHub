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
    private List<Map<String, Object>> links;

    private Integer statusCode;
    private String statusMessage;

    private Map<String, Object> resource;
    private Map<String, Object> scope;

    private String model;
    private Long inputTokens;
    private Long outputTokens;
    private Long totalTokens;
    private String conversationId;

    private String runId;
    private String agentId;

    private String operationName;
    private String serviceName;
    private Long startTimestamp;
    private Long endTimestamp;
    private Long duration;
    private String status;
    private String statusDescription;

    private String tenantId;
    private String workspaceId;
    private Instant createdAt;
}
