package com.agenthub.application.usecase;

import com.agenthub.application.dto.MetricOutput;
import com.agenthub.application.port.out.repositories.MetricRepository;
import com.agenthub.domain.model.monitor.Metric;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Metric UseCase.
 */
@Component
@RequiredArgsConstructor
public class MetricUseCase {
    private final MetricRepository repository;

    public MetricOutput create(
        String metricType,
        String metricName,
        Double metricValue
    ) {
        Metric metric = Metric.create(metricType, metricName, metricValue);
        Metric saved = repository.save(metric);
        return toOutput(saved);
    }

    public List<MetricOutput> listByRun(String runId) {
        return repository.findByRunId(runId).stream()
            .map(this::toOutput)
            .toList();
    }

    public List<MetricOutput> listByAgent(String agentId) {
        return repository.findByAgentId(agentId).stream()
            .map(this::toOutput)
            .toList();
    }

    public List<MetricOutput> listByType(String metricType) {
        return repository.findByMetricType(metricType).stream()
            .map(this::toOutput)
            .toList();
    }

    public List<MetricOutput> list() {
        return repository.findAll().stream()
            .map(this::toOutput)
            .toList();
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private MetricOutput toOutput(Metric metric) {
        return new MetricOutput(
            metric.getId(),
            metric.getMetricType(),
            metric.getMetricName(),
            metric.getMetricValue(),
            metric.getRunId(),
            metric.getAgentId(),
            metric.getTraceId(),
            metric.getSpanId(),
            metric.getLabels(),
            metric.getTimestamp(),
            metric.getCreatedAt()
        );
    }
}
