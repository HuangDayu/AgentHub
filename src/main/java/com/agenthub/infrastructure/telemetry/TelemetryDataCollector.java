package com.agenthub.infrastructure.telemetry;

import com.agenthub.application.port.out.*;
import com.agenthub.domain.model.telemetry.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 遥测数据收集器
 * 直接在应用内部收集和存储追踪数据，绕过HTTP endpoint
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryDataCollector {
    private final OtlpSpanRepository spanRepository;
    private final OtlpMetricRepository metricRepository;
    private final OtlpLogRepository logRepository;
    
    private final Map<String, OtlpSpan> activeSpans = new ConcurrentHashMap<>();

    /**
     * 开始一个Span
     */
    public String startSpan(String operationName, String serviceName, String traceId) {
        String spanId = generateSpanId();
        OtlpSpan span = new OtlpSpan();
        span.setSpanId(spanId);
        span.setTraceId(traceId);
        span.setOperationName(operationName);
        span.setServiceName(serviceName);
        span.setKind("INTERNAL");
        span.setStartTimestamp(System.nanoTime());
        span.setStatus("UNSET");
        
        activeSpans.put(spanId, span);
        log.debug("Started span: {} [{}]", operationName, spanId);
        return spanId;
    }

    /**
     * 结束Span并存储
     */
    public void endSpan(String spanId) {
        OtlpSpan span = activeSpans.remove(spanId);
        if (span == null) {
            log.warn("Span not found: {}", spanId);
            return;
        }
        
        span.setEndTimestamp(System.nanoTime());
        if (span.getStartTimestamp() != null && span.getEndTimestamp() != null) {
            span.setDuration(span.getEndTimestamp() - span.getStartTimestamp());
        }
        span.setCreatedAt(Instant.now());
        
        spanRepository.save(span);
        log.debug("Ended and stored span: {} [{}]", span.getOperationName(), spanId);
    }

    /**
     * 记录Span错误
     */
    public void recordError(String spanId, String errorMessage) {
        OtlpSpan span = activeSpans.get(spanId);
        if (span != null) {
            span.setStatus("ERROR");
            span.setStatusDescription(errorMessage);
        }
    }

    /**
     * 记录指标
     */
    public void recordMetric(String name, String type, String serviceName, Object value) {
        OtlpMetric metric = new OtlpMetric();
        metric.setMetricName(name);
        metric.setMetricType(type);
        metric.setServiceName(serviceName);
        metric.setValue(String.valueOf(value));
        metric.setTimestamp(System.nanoTime());
        metric.setCreatedAt(Instant.now());
        
        metricRepository.save(metric);
        log.debug("Recorded metric: {} = {}", name, value);
    }

    /**
     * 记录日志
     */
    public void recordLog(String serviceName, String severity, String body) {
        OtlpLog logEntry = new OtlpLog();
        logEntry.setLogId(generateLogId());
        logEntry.setServiceName(serviceName);
        logEntry.setSeverity(severity);
        logEntry.setBody(body);
        logEntry.setTimestamp(System.nanoTime());
        logEntry.setCreatedAt(Instant.now());
        
        logRepository.save(logEntry);
        log.debug("Recorded log: [{}] {}", severity, body);
    }

    private String generateSpanId() {
        return "span-" + System.nanoTime() + "-" + Thread.currentThread().getId();
    }

    private String generateLogId() {
        return "log-" + System.nanoTime() + "-" + Thread.currentThread().getId();
    }
}
