package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.trace.Trace;

import java.util.List;
import java.util.Optional;

/**
 * Trace Repository 接口.
 */
public interface TraceRepository {
    Trace save(Trace trace);

    Optional<Trace> findById(String id);

    Optional<Trace> findByTraceId(String traceId);

    List<Trace> findByRunId(String runId);

    List<Trace> findAll();

    void deleteById(String id);
}
