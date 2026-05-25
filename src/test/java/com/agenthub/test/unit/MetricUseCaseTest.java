package com.agenthub.test.unit;

import com.agenthub.application.dto.MetricOutput;
import com.agenthub.application.port.out.repositories.MetricRepository;
import com.agenthub.application.usecase.MetricUseCase;
import com.agenthub.domain.model.monitor.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MetricUseCase 单元测试.
 */
class MetricUseCaseTest {

    @Mock
    private MetricRepository repository;

    private MetricUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new MetricUseCase(repository);
    }

    @Test
    void shouldCreateMetric() {
        Metric metric = Metric.create("LATENCY_NS", "latency", 100.0);
        when(repository.save(any())).thenReturn(metric);

        MetricOutput result = useCase.create("LATENCY_NS", "latency", 100.0);

        assertNotNull(result);
        assertEquals("LATENCY_NS", result.getMetricType());
        assertEquals("latency", result.getMetricName());
        assertEquals(100.0, result.getMetricValue());
    }

    @Test
    void shouldListMetricsByRun() {
        Metric metric = Metric.create("LATENCY_NS", "latency", 100.0);
        when(repository.findByRunId("run-1")).thenReturn(List.of(metric));

        List<MetricOutput> result = useCase.listByRun("run-1");

        assertEquals(1, result.size());
        assertEquals("LATENCY_NS", result.get(0).getMetricType());
    }

    @Test
    void shouldListMetricsByAgent() {
        Metric metric = Metric.create("LATENCY_NS", "latency", 100.0);
        when(repository.findByAgentId("agent-1")).thenReturn(List.of(metric));

        List<MetricOutput> result = useCase.listByAgent("agent-1");

        assertEquals(1, result.size());
    }

    @Test
    void shouldListMetricsByType() {
        Metric metric = Metric.create("LATENCY_NS", "latency", 100.0);
        when(repository.findByMetricType("LATENCY_NS")).thenReturn(List.of(metric));

        List<MetricOutput> result = useCase.listByType("LATENCY_NS");

        assertEquals(1, result.size());
    }

    @Test
    void shouldListAllMetrics() {
        Metric metric = Metric.create("LATENCY_NS", "latency", 100.0);
        when(repository.findAll()).thenReturn(List.of(metric));

        List<MetricOutput> result = useCase.list();

        assertEquals(1, result.size());
    }

    @Test
    void shouldDeleteMetric() {
        doNothing().when(repository).deleteById("id-1");

        useCase.delete("id-1");

        verify(repository).deleteById("id-1");
    }
}
