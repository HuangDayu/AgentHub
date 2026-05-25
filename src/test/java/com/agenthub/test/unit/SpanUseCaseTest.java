package com.agenthub.test.unit;

import com.agenthub.application.dto.SpanOutput;
import com.agenthub.application.port.out.repositories.SpanRepository;
import com.agenthub.application.usecase.SpanUseCase;
import com.agenthub.domain.model.trace.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SpanUseCase 单元测试.
 */
class SpanUseCaseTest {

    @Mock
    private SpanRepository repository;

    private SpanUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new SpanUseCase(repository);
    }

    @Test
    void shouldGetSpanById() {
        Span span = createTestSpan();
        when(repository.findBySpanId("span-1")).thenReturn(Optional.of(span));

        SpanOutput result = useCase.get("span-1");

        assertNotNull(result);
        assertEquals("span-1", result.getSpanId());
        verify(repository).findBySpanId("span-1");
    }

    @Test
    void shouldThrowExceptionWhenSpanNotFound() {
        when(repository.findBySpanId(anyString())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> useCase.get("non-existent"));
    }

    @Test
    void shouldListSpansByTrace() {
        Span span = createTestSpan();
        when(repository.findByTraceId("trace-1")).thenReturn(List.of(span));

        List<SpanOutput> result = useCase.listByTrace("trace-1");

        assertEquals(1, result.size());
        assertEquals("span-1", result.get(0).getSpanId());
    }

    @Test
    void shouldListSpansByRun() {
        Span span = createTestSpan();
        when(repository.findByRunId("run-1")).thenReturn(List.of(span));

        List<SpanOutput> result = useCase.listByRun("run-1");

        assertEquals(1, result.size());
    }

    @Test
    void shouldListAllSpans() {
        Span span = createTestSpan();
        when(repository.findAll()).thenReturn(List.of(span));

        List<SpanOutput> result = useCase.list();

        assertEquals(1, result.size());
    }

    @Test
    void shouldDeleteSpan() {
        Span span = createTestSpan();
        when(repository.findBySpanId("span-1")).thenReturn(Optional.of(span));
        doNothing().when(repository).deleteById(anyString());

        useCase.delete("span-1");

        verify(repository).deleteById(span.getId());
    }

    private Span createTestSpan() {
        return Span.create("trace-1", "span-1", "test-span");
    }
}
