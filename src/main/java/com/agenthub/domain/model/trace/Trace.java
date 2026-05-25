package com.agenthub.domain.model.trace;

import lombok.Data;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * Trace 领域模型.
 * 表示一次完整执行的追踪.
 */
@Data
public class Trace {
    private String id;
    private String traceId;
    private String runId;

    private String rootSpanId;
    private Integer spanCount;

    private String startTimeUnixNano;
    private String endTimeUnixNano;
    private Long durationNs;

    private Integer statusCode;
    private String errorMessage;

    private Integer totalTokens;

    private String tenantId;
    private String workspaceId;
    private Instant createdAt;

    public Trace() {
        this.id = randomId();
        this.createdAt = Instant.now();
    }

    public static Trace create(String traceId, String runId) {
        Trace trace = new Trace();
        trace.traceId = traceId;
        trace.runId = runId;
        return trace;
    }

    public boolean hasError() {
        return statusCode != null && statusCode == 2;
    }
}
