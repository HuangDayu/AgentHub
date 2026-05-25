package com.agenthub.domain.model.trace;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * Span 领域模型.
 * 表示一个操作单元的追踪数据.
 */
@Data
public class Span {
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

    public Span() {
        this.id = randomId();
        this.createdAt = Instant.now();
    }

    public static Span create(String traceId, String spanId, String name) {
        Span span = new Span();
        span.traceId = traceId;
        span.spanId = spanId;
        span.name = name;
        return span;
    }

    public boolean isRoot() {
        return parentSpanId == null || parentSpanId.isEmpty();
    }

    public boolean hasError() {
        return statusCode != null && statusCode == 2;
    }
}
