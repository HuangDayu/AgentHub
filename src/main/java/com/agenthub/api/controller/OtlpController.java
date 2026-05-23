package com.agenthub.api.controller;

import com.agenthub.api.dto.OtlpLogResponse;
import com.agenthub.api.dto.OtlpMetricResponse;
import com.agenthub.api.dto.OtlpSpanResponse;
import com.agenthub.application.dto.*;
import com.agenthub.application.parser.OtlpDataParser;
import com.agenthub.application.usecase.OtlpUseCase;
import com.agenthub.domain.model.telemetry.OtlpLog;
import com.agenthub.domain.model.telemetry.OtlpMetric;
import com.agenthub.domain.model.telemetry.OtlpSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OTLP数据接收和查询控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/otlp")
@RequiredArgsConstructor
public class OtlpController {
    private final OtlpUseCase useCase;
    private final OtlpDataParser dataParser;

    @PostMapping("/traces")
    public ResponseEntity<Map<String, Object>> receiveTraces(@RequestBody Map<String, Object> traceData) {
        log.debug("Received trace data");
        List<OtlpSpan> spans = dataParser.parseTraceData(traceData);
        useCase.storeSpans(spans);
        return buildSuccessResponse("Traces received and stored", spans.size());
    }

    @PostMapping("/metrics")
    public ResponseEntity<Map<String, Object>> receiveMetrics(@RequestBody Map<String, Object> metricData) {
        log.debug("Received metric data");
        List<OtlpMetric> metrics = dataParser.parseMetricData(metricData);
        useCase.storeMetrics(metrics);
        return buildSuccessResponse("Metrics received and stored", metrics.size());
    }

    @PostMapping("/logs")
    public ResponseEntity<Map<String, Object>> receiveLogs(@RequestBody Map<String, Object> logData) {
        log.debug("Received log data");
        List<OtlpLog> logs = dataParser.parseLogData(logData);
        useCase.storeLogs(logs);
        return buildSuccessResponse("Logs received and stored", logs.size());
    }

    @GetMapping("/spans")
    public List<OtlpSpanResponse> querySpans(@RequestParam(defaultValue = "100") int limit) {
        List<OtlpSpanOutput> outputs = useCase.queryRecentSpans(limit);
        return convertSpans(outputs);
    }

    @GetMapping("/traces/{traceId}")
    public List<OtlpSpanResponse> queryTraceById(@PathVariable String traceId) {
        List<OtlpSpanOutput> outputs = useCase.querySpansByTraceId(traceId);
        return convertSpans(outputs);
    }

    @GetMapping("/metrics/query")
    public List<OtlpMetricResponse> queryMetrics(@RequestParam(defaultValue = "100") int limit) {
        List<OtlpMetricOutput> outputs = useCase.queryRecentMetrics(limit);
        return convertMetrics(outputs);
    }

    @GetMapping("/logs/query")
    public List<OtlpLogResponse> queryLogs(@RequestParam(defaultValue = "100") int limit) {
        List<OtlpLogOutput> outputs = useCase.queryRecentLogs(limit);
        return convertLogs(outputs);
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return useCase.getStatistics();
    }

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(String message, int count) {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", message,
            "count", count
        ));
    }

    private List<OtlpSpanResponse> convertSpans(List<OtlpSpanOutput> outputs) {
        return outputs.stream().map(this::toSpanResponse).toList();
    }

    private OtlpSpanResponse toSpanResponse(OtlpSpanOutput output) {
        OtlpSpanResponse response = new OtlpSpanResponse();
        response.setSpanId(output.getSpanId());
        response.setTraceId(output.getTraceId());
        response.setParentSpanId(output.getParentSpanId());
        response.setOperationName(output.getOperationName());
        response.setServiceName(output.getServiceName());
        response.setKind(output.getKind());
        response.setStartTimestamp(output.getStartTimestamp());
        response.setEndTimestamp(output.getEndTimestamp());
        response.setDuration(output.getDuration());
        response.setStatus(output.getStatus());
        response.setStatusDescription(output.getStatusDescription());
        response.setAttributes(output.getAttributes());
        response.setEvents(output.getEvents());
        response.setLinks(output.getLinks());
        response.setCreatedAt(output.getCreatedAt());
        return response;
    }

    private List<OtlpMetricResponse> convertMetrics(List<OtlpMetricOutput> outputs) {
        return outputs.stream().map(this::toMetricResponse).toList();
    }

    private OtlpMetricResponse toMetricResponse(OtlpMetricOutput output) {
        OtlpMetricResponse response = new OtlpMetricResponse();
        response.setMetricName(output.getMetricName());
        response.setDescription(output.getDescription());
        response.setUnit(output.getUnit());
        response.setMetricType(output.getMetricType());
        response.setServiceName(output.getServiceName());
        response.setValue(output.getValue());
        response.setAttributes(output.getAttributes());
        response.setTimestamp(output.getTimestamp());
        response.setCreatedAt(output.getCreatedAt());
        return response;
    }

    private List<OtlpLogResponse> convertLogs(List<OtlpLogOutput> outputs) {
        return outputs.stream().map(this::toLogResponse).toList();
    }

    private OtlpLogResponse toLogResponse(OtlpLogOutput output) {
        OtlpLogResponse response = new OtlpLogResponse();
        response.setLogId(output.getLogId());
        response.setTraceId(output.getTraceId());
        response.setSpanId(output.getSpanId());
        response.setServiceName(output.getServiceName());
        response.setSeverity(output.getSeverity());
        response.setSeverityNumber(output.getSeverityNumber());
        response.setBody(output.getBody());
        response.setAttributes(output.getAttributes());
        response.setTimestamp(output.getTimestamp());
        response.setCreatedAt(output.getCreatedAt());
        return response;
    }
}
