package com.agenthub.domain.model.monitor;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * Metric 领域模型.
 * 表示监控指标数据.
 */
@Data
public class Metric {
    private String id;
    private String metricType;
    private String metricName;

    private Double metricValue;

    private String runId;
    private String agentId;
    private String traceId;
    private String spanId;

    private Map<String, Object> labels;

    private Instant timestamp;

    private String tenantId;
    private String workspaceId;
    private Instant createdAt;

    public Metric() {
        this.id = randomId();
        this.timestamp = Instant.now();
        this.createdAt = Instant.now();
    }

    public static Metric create(String metricType, String metricName, Double value) {
        Metric metric = new Metric();
        metric.metricType = metricType;
        metric.metricName = metricName;
        metric.metricValue = value;
        return metric;
    }
}
