package com.agenthub.application.port.out;
import com.agenthub.domain.model.telemetry.OtlpSpan;

import java.util.List;

/**
 * OTLP Span仓储接口
 */
public interface OtlpSpanRepository {
    void save(OtlpSpan span);
    List<OtlpSpan> findRecent(int limit);
    List<OtlpSpan> findByTraceId(String traceId);
    List<OtlpSpan> findByServiceName(String serviceName, int limit);
    long count();
}
