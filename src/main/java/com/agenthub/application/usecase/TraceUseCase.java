package com.agenthub.application.usecase;

import com.agenthub.application.dto.TraceOutput;
import com.agenthub.application.port.out.repositories.TraceRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.trace.Trace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Trace UseCase.
 */
@Component
@RequiredArgsConstructor
public class TraceUseCase {
    private final TraceRepository repository;

    public TraceOutput get(String traceId) {
        return repository.findByTraceId(traceId)
            .map(this::toOutput)
            .orElseThrow(() -> notFound(traceId));
    }

    public List<TraceOutput> listByRun(String runId) {
        return repository.findByRunId(runId).stream()
            .map(this::toOutput)
            .toList();
    }

    public List<TraceOutput> list() {
        return repository.findAll().stream()
            .map(this::toOutput)
            .toList();
    }

    public void delete(String traceId) {
        repository.findByTraceId(traceId)
            .ifPresent(trace -> repository.deleteById(trace.getId()));
    }

    private TraceOutput toOutput(Trace trace) {
        return new TraceOutput(
            trace.getId(),
            trace.getTraceId(),
            trace.getRunId(),
            trace.getRootSpanId(),
            trace.getSpanCount(),
            trace.getStartTimeUnixNano(),
            trace.getEndTimeUnixNano(),
            trace.getDurationNs(),
            trace.getStatusCode(),
            trace.getErrorMessage(),
            trace.getTotalTokens(),
            trace.getCreatedAt()
        );
    }

    private NotFoundException notFound(String traceId) {
        return new NotFoundException("Trace not found: " + traceId);
    }
}
