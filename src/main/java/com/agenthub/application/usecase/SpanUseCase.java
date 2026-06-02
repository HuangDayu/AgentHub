package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.dto.SpanOutput;
import com.agenthub.application.port.out.repositories.SpanRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.trace.Span;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Span UseCase.
 */
@Component
@RequiredArgsConstructor
public class SpanUseCase {
    private final SpanRepository repository;

    public SpanOutput get(String spanId) {
        return repository.findBySpanId(spanId)
            .map(this::toOutput)
            .orElseThrow(() -> notFound(spanId));
    }

    public List<SpanOutput> listByTrace(String traceId) {
        return repository.findByTraceId(traceId).stream()
            .map(this::toOutput)
            .toList();
    }

    public List<SpanOutput> listByRun(String runId) {
        return repository.findByRunId(runId).stream()
            .map(this::toOutput)
            .toList();
    }

    public void delete(String spanId) {
        repository.findBySpanId(spanId)
            .ifPresent(span -> repository.deleteById(span.getId()));
    }

    private SpanOutput toOutput(Span span) {
        return BeanUtil.copyProperties(span, SpanOutput.class);
    }

    private NotFoundException notFound(String spanId) {
        return new NotFoundException("Span not found: " + spanId);
    }
}
