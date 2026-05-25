package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.trace.Span;

import java.util.List;
import java.util.Optional;

/**
 * Span Repository 接口.
 */
public interface SpanRepository {
    Span save(Span span);

    Optional<Span> findById(String id);

    Optional<Span> findBySpanId(String spanId);

    List<Span> findByTraceId(String traceId);

    List<Span> findByRunId(String runId);

    List<Span> findAll();

    void deleteById(String id);
}
