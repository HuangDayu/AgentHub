package com.agenthub.application.parser;

import com.agenthub.domain.model.telemetry.OtlpLog;
import com.agenthub.domain.model.telemetry.OtlpMetric;
import com.agenthub.domain.model.telemetry.OtlpSpan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * OTLP数据解析器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtlpDataParser {
    private final ObjectMapper objectMapper;

    public List<OtlpSpan> parseTraceData(Map<String, Object> traceData) {
        return tryParseStandardFormat(traceData);
    }

    private List<OtlpSpan> tryParseStandardFormat(Map<String, Object> traceData) {
        try {
            return parseResourceSpans(traceData);
        } catch (Exception e) {
            log.debug("Trying simple format: {}", e.getMessage());
            return parseSimpleFormat(traceData);
        }
    }

    private List<OtlpSpan> parseResourceSpans(Map<String, Object> traceData) {
        if (!traceData.containsKey("resourceSpans")) {
            throw new IllegalArgumentException("No resourceSpans found");
        }
        List<Map<String, Object>> resourceSpans = getList(traceData, "resourceSpans");
        return resourceSpans.stream()
            .flatMap(rs -> getList(rs, "scopeSpans").stream())
            .flatMap(ss -> getList(ss, "spans").stream())
            .map(this::mapToSpan)
            .toList();
    }

    private List<OtlpSpan> parseSimpleFormat(Map<String, Object> traceData) {
        if (traceData.containsKey("spans")) {
            return getList(traceData, "spans").stream()
                .map(this::mapToSpan)
                .toList();
        }
        return List.of(mapToSpan(traceData));
    }

    public OtlpSpan mapToSpan(Map<String, Object> map) {
        OtlpSpan span = new OtlpSpan();
        setSpanIds(span, map);
        setSpanDetails(span, map);
        setSpanTimestamps(span, map);
        setSpanMetadata(span, map);
        return span;
    }

    private void setSpanIds(OtlpSpan span, Map<String, Object> map) {
        span.setSpanId(getStringWithAltKey(map, "spanId", "span_id"));
        span.setTraceId(getStringWithAltKey(map, "traceId", "trace_id"));
        span.setParentSpanId(getStringWithAltKey(map, "parentSpanId", "parent_span_id"));
    }

    private void setSpanDetails(OtlpSpan span, Map<String, Object> map) {
        span.setOperationName(getStringWithAltKey(map, "operationName", "operation_name"));
        span.setServiceName(getStringWithAltKey(map, "serviceName", "service_name"));
        span.setKind(getStringWithDefault(map, "kind", "INTERNAL"));
        span.setStatus(getStringWithDefault(map, "status", "UNSET"));
        span.setStatusDescription((String) map.get("statusDescription"));
    }

    private void setSpanTimestamps(OtlpSpan span, Map<String, Object> map) {
        span.setStartTimestamp(getLong(map, "startTimestamp"));
        span.setEndTimestamp(getLong(map, "endTimestamp"));
        calculateDuration(span);
    }

    private void calculateDuration(OtlpSpan span) {
        if (span.getStartTimestamp() != null && span.getEndTimestamp() != null) {
            span.setDuration(span.getEndTimestamp() - span.getStartTimestamp());
        }
    }

    private void setSpanMetadata(OtlpSpan span, Map<String, Object> map) {
        setJsonField(span::setAttributes, map.get("attributes"));
        setJsonField(span::setEvents, map.get("events"));
        setJsonField(span::setLinks, map.get("links"));
    }

    public List<OtlpMetric> parseMetricData(Map<String, Object> metricData) {
        if (metricData.containsKey("metrics")) {
            return getList(metricData, "metrics").stream()
                .map(this::mapToMetric)
                .toList();
        }
        return List.of(mapToMetric(metricData));
    }

    public OtlpMetric mapToMetric(Map<String, Object> map) {
        OtlpMetric metric = new OtlpMetric();
        setMetricDetails(metric, map);
        setMetricMetadata(metric, map);
        return metric;
    }

    private void setMetricDetails(OtlpMetric metric, Map<String, Object> map) {
        metric.setMetricName(getStringWithAltKey(map, "metricName", "name"));
        metric.setDescription((String) map.get("description"));
        metric.setUnit((String) map.get("unit"));
        metric.setMetricType(getStringWithAltKey(map, "metricType", "type"));
        metric.setServiceName(getStringWithAltKey(map, "serviceName", "service_name"));
    }

    private void setMetricMetadata(OtlpMetric metric, Map<String, Object> map) {
        setJsonField(metric::setValue, map.get("value"));
        setJsonField(metric::setAttributes, map.get("attributes"));
        metric.setTimestamp(getLong(map, "timestamp"));
    }

    public List<OtlpLog> parseLogData(Map<String, Object> logData) {
        if (logData.containsKey("logs")) {
            return getList(logData, "logs").stream()
                .map(this::mapToLog)
                .toList();
        }
        return List.of(mapToLog(logData));
    }

    public OtlpLog mapToLog(Map<String, Object> map) {
        OtlpLog log = new OtlpLog();
        setLogDetails(log, map);
        setLogMetadata(log, map);
        return log;
    }

    private void setLogDetails(OtlpLog log, Map<String, Object> map) {
        log.setLogId(getStringWithAltKey(map, "logId", "id"));
        log.setTraceId((String) map.get("traceId"));
        log.setSpanId((String) map.get("spanId"));
        log.setServiceName(getStringWithAltKey(map, "serviceName", "service_name"));
        log.setSeverity(getStringWithDefault(map, "severity", "INFO"));
        log.setSeverityNumber(getInteger(map, "severityNumber"));
        log.setBody(getStringWithAltKey(map, "body", "message"));
    }

    private void setLogMetadata(OtlpLog log, Map<String, Object> map) {
        setJsonField(log::setAttributes, map.get("attributes"));
        log.setTimestamp(getLong(map, "timestamp"));
    }

    private String getStringWithAltKey(Map<String, Object> map, String key1, String key2) {
        return (String) map.getOrDefault(key1, map.get(key2));
    }

    private String getStringWithDefault(Map<String, Object> map, String key, String defaultValue) {
        return (String) map.getOrDefault(key, defaultValue);
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? ((Number) value).longValue() : null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? ((Number) value).intValue() : null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(Map<String, Object> map, String key) {
        return (List<Map<String, Object>>) map.getOrDefault(key, List.of());
    }

    private void setJsonField(java.util.function.Consumer<String> setter, Object value) {
        if (value != null) {
            try {
                setter.accept(objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize value to JSON: {}", e.getMessage());
            }
        }
    }
}
