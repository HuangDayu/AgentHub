package com.agenthub.application.usecase;

import com.agenthub.application.dto.*;
import com.agenthub.application.port.out.repositories.OtlpLogRepository;
import com.agenthub.application.port.out.repositories.OtlpMetricRepository;
import com.agenthub.application.port.out.repositories.OtlpSpanRepository;
import com.agenthub.domain.model.telemetry.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * OTLP数据管理用例
 */
@Service
@RequiredArgsConstructor
public class OtlpUseCase {
    private final OtlpSpanRepository spanRepository;
    private final OtlpMetricRepository metricRepository;
    private final OtlpLogRepository logRepository;

    public void storeSpan(OtlpSpan span) {
        spanRepository.save(span);
    }

    public void storeSpans(List<OtlpSpan> spans) {
        spans.forEach(this::storeSpan);
    }

    public void storeMetric(OtlpMetric metric) {
        metricRepository.save(metric);
    }

    public void storeMetrics(List<OtlpMetric> metrics) {
        metrics.forEach(this::storeMetric);
    }

    public void storeLog(OtlpLog log) {
        logRepository.save(log);
    }

    public void storeLogs(List<OtlpLog> logs) {
        logs.forEach(this::storeLog);
    }

    public List<OtlpSpanOutput> queryRecentSpans(int limit) {
        return spanRepository.findRecent(limit).stream()
            .map(this::toSpanOutput)
            .toList();
    }

    public List<OtlpSpanOutput> querySpansByTraceId(String traceId) {
        return spanRepository.findByTraceId(traceId).stream()
            .map(this::toSpanOutput)
            .toList();
    }

    public List<OtlpMetricOutput> queryRecentMetrics(int limit) {
        return metricRepository.findRecent(limit).stream()
            .map(this::toMetricOutput)
            .toList();
    }

    public List<OtlpLogOutput> queryRecentLogs(int limit) {
        return logRepository.findRecent(limit).stream()
            .map(this::toLogOutput)
            .toList();
    }

    public Map<String, Object> getStatistics() {
        return Map.of(
            "totalSpans", spanRepository.count(),
            "totalMetrics", metricRepository.count(),
            "totalLogs", logRepository.count()
        );
    }

    private OtlpSpanOutput toSpanOutput(OtlpSpan span) {
        OtlpSpanOutput output = new OtlpSpanOutput();
        output.setSpanId(span.getSpanId());
        output.setTraceId(span.getTraceId());
        output.setParentSpanId(span.getParentSpanId());
        output.setOperationName(span.getOperationName());
        output.setServiceName(span.getServiceName());
        output.setKind(span.getKind());
        output.setStartTimestamp(span.getStartTimestamp());
        output.setEndTimestamp(span.getEndTimestamp());
        output.setDuration(span.getDuration());
        output.setStatus(span.getStatus());
        output.setStatusDescription(span.getStatusDescription());
        output.setAttributes(span.getAttributes());
        output.setEvents(span.getEvents());
        output.setLinks(span.getLinks());
        output.setCreatedAt(span.getCreatedAt());
        return output;
    }

    private OtlpMetricOutput toMetricOutput(OtlpMetric metric) {
        OtlpMetricOutput output = new OtlpMetricOutput();
        output.setMetricName(metric.getMetricName());
        output.setDescription(metric.getDescription());
        output.setUnit(metric.getUnit());
        output.setMetricType(metric.getMetricType());
        output.setServiceName(metric.getServiceName());
        output.setValue(metric.getValue());
        output.setAttributes(metric.getAttributes());
        output.setTimestamp(metric.getTimestamp());
        output.setCreatedAt(metric.getCreatedAt());
        return output;
    }

    private OtlpLogOutput toLogOutput(OtlpLog log) {
        OtlpLogOutput output = new OtlpLogOutput();
        output.setLogId(log.getLogId());
        output.setTraceId(log.getTraceId());
        output.setSpanId(log.getSpanId());
        output.setServiceName(log.getServiceName());
        output.setSeverity(log.getSeverity());
        output.setSeverityNumber(log.getSeverityNumber());
        output.setBody(log.getBody());
        output.setAttributes(log.getAttributes());
        output.setTimestamp(log.getTimestamp());
        output.setCreatedAt(log.getCreatedAt());
        return output;
    }
}
