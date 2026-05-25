package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.monitor.Metric;

import java.util.List;

/**
 * Metric Repository 接口.
 */
public interface MetricRepository {
    Metric save(Metric metric);

    List<Metric> findByRunId(String runId);

    List<Metric> findByAgentId(String agentId);

    List<Metric> findByMetricType(String metricType);

    List<Metric> findAll();

    void deleteById(String id);
}
