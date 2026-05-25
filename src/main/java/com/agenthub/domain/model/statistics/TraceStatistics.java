package com.agenthub.domain.model.statistics;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * TraceStatistics 领域模型.
 * 表示追踪统计数据.
 */
@Data
public class TraceStatistics {
    private String id;
    private String runId;

    private Long totalTraces;
    private Long totalSpans;
    private Long errorTraces;
    private Double avgDuration;
    private Long totalTokens;

    private List<Map<String, Object>> tracesByStatus;
    private List<Map<String, Object>> spansByModel;

    private Instant startTime;
    private Instant endTime;

    private String tenantId;
    private String workspaceId;
    private Instant createdAt;

    public TraceStatistics() {
        this.id = randomId();
        this.createdAt = Instant.now();
    }
}
