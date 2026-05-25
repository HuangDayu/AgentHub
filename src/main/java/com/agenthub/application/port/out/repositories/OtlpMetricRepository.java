package com.agenthub.application.port.out.repositories;
import com.agenthub.domain.model.telemetry.OtlpMetric;

import java.util.List;

/**
 * OTLP Metric仓储接口
 */
public interface OtlpMetricRepository {
    void save(OtlpMetric metric);
    List<OtlpMetric> findRecent(int limit);
    long count();
}
