package com.agenthub.test.unit;

import com.agenthub.application.dto.TraceOutput;
import com.agenthub.application.port.out.repositories.TraceRepository;
import com.agenthub.application.usecase.TraceUseCase;
import com.agenthub.domain.model.trace.Trace;
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
 * TraceUseCase 单元测试.
 */
class TraceUseCaseTest {

    @Mock
    private TraceRepository repository;

    private TraceUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new TraceUseCase(repository);
    }

    @Test
    void shouldGetTraceById() {
        Trace trace = createTestTrace();
        when(repository.findByTraceId("trace-1")).thenReturn(Optional.of(trace));

        TraceOutput result = useCase.get("trace-1");

        assertNotNull(result);
        assertEquals("trace-1", result.getTraceId());
        verify(repository).findByTraceId("trace-1");
    }

    @Test
    void shouldThrowExceptionWhenTraceNotFound() {
        when(repository.findByTraceId(anyString())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> useCase.get("non-existent"));
    }

    @Test
    void shouldListTracesByRun() {
        Trace trace = createTestTrace();
        when(repository.findByRunId("run-1")).thenReturn(List.of(trace));

        List<TraceOutput> result = useCase.listByRun("run-1");

        assertEquals(1, result.size());
        assertEquals("trace-1", result.get(0).getTraceId());
    }

    @Test
    void shouldListAllTraces() {
        Trace trace = createTestTrace();
        when(repository.findAll()).thenReturn(List.of(trace));

        List<TraceOutput> result = useCase.list();

        assertEquals(1, result.size());
    }

    @Test
    void shouldDeleteTrace() {
        Trace trace = createTestTrace();
        when(repository.findByTraceId("trace-1")).thenReturn(Optional.of(trace));
        doNothing().when(repository).deleteById(anyString());

        useCase.delete("trace-1");

        verify(repository).deleteById(trace.getId());
    }

    private Trace createTestTrace() {
        return Trace.create("trace-1", "run-1");
    }
}
