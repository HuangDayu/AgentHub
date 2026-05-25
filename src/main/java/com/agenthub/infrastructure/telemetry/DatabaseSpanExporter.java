package com.agenthub.infrastructure.telemetry;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.application.port.out.repositories.SpanRepository;
import com.agenthub.common.annotations.IgnoreTenantContext;
import com.agenthub.domain.model.agent.Session;
import com.agenthub.domain.model.trace.Span;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.agentscope.core.tracing.telemetry.GenAiIncubatingAttributes.*;
import static org.springframework.ai.util.json.JsonParser.fromJson;
import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * 数据库Span导出器
 * 实现OpenTelemetry SDK的SpanExporter接口
 * 直接将Span数据导出到数据库，无需HTTP调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSpanExporter implements SpanExporter {
    private final SpanRepository spanRepository;
    private final SessionRepository sessionRepository;
    private final TimedCache<String, Session> timedCache = CacheUtil.newTimedCache(TimeUnit.MINUTES.toMillis(10));

    @IgnoreTenantContext
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        try {
            spans.forEach(this::exportSpan);
            log.debug("Exported {} spans to database", spans.size());
            return CompletableResultCode.ofSuccess();
        } catch (Exception e) {
            log.error("Failed to export spans: {}", e.getMessage(), e);
            return CompletableResultCode.ofFailure();
        }
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    private void exportSpan(SpanData spanData) {
        Span span = convertToSpan(spanData);
        spanRepository.save(span);
    }

    private Span convertToSpan(SpanData spanData) {
        Span span = new Span();
        setIdentity(span, spanData);
        setTimestamps(span, spanData);
        setStatus(span, spanData);
        setTelemetry(span, spanData);
        setSessionInfo(span, spanData);
        return span;
    }

    private void setIdentity(Span span, SpanData spanData) {
        span.setSpanId(spanData.getSpanId());
        span.setTraceId(spanData.getTraceId());
        span.setParentSpanId(spanData.getParentSpanId());
        span.setName(spanData.getName());
        span.setOperationName(spanData.getName());
        span.setKind(spanData.getKind().name());
        span.setServiceName(getAttributeValue(spanData, GEN_AI_AGENT_NAME));
    }

    private void setSessionInfo(Span span, SpanData spanData) {
        span.setAgentId(getAttributeValue(spanData, AGENTHUB_AGENT_ID));
        span.setWorkspaceId(getAttributeValue(spanData, AGENTHUB_WORKSPACE_ID));
        span.setTenantId(getAttributeValue(spanData, AGENTHUB_TENANT_ID));
        span.setRunId(getAttributeValue(spanData, AGENTHUB_SESSION_ID));
    }

    private void setTimestamps(Span span, SpanData spanData) {
        span.setStartTimeUnixNano(String.valueOf(spanData.getStartEpochNanos()));
        span.setEndTimeUnixNano(String.valueOf(spanData.getEndEpochNanos()));
        span.setStartTimestamp(spanData.getStartEpochNanos());
        span.setEndTimestamp(spanData.getEndEpochNanos());
        span.setLatencyNs(spanData.getEndEpochNanos() - spanData.getStartEpochNanos());
        span.setDuration(span.getLatencyNs());
    }

    private void setStatus(Span span, SpanData spanData) {
        span.setStatus(spanData.getStatus().getStatusCode().name());
        span.setStatusCode(statusCode(spanData));
        span.setStatusDescription(spanData.getStatus().getDescription());
        span.setStatusMessage(spanData.getStatus().getDescription());
    }

    private Integer statusCode(SpanData spanData) {
        return switch (spanData.getStatus().getStatusCode()) {
            case OK -> 1;
            case ERROR -> 2;
            default -> 0;
        };
    }

    private void setTelemetry(Span span, SpanData spanData) {
        span.setAttributes(attributes(spanData.getAttributes()));
        span.setResource(attributes(spanData.getResource().getAttributes()));
        span.setEvents(fromJson(toJson(spanData.getEvents()), new TypeReference<>() {
        }));
        span.setLinks(fromJson(toJson(spanData.getLinks()), new TypeReference<>() {
        }));
        setRuntimeFields(span, spanData);
        setTokens(span, spanData);
    }

    private void setRuntimeFields(Span span, SpanData spanData) {
        span.setModel(getAttributeValue(spanData, GEN_AI_REQUEST_MODEL));
        span.setConversationId(getAttributeValue(spanData, GEN_AI_CONVERSATION_ID));
        span.setRunId(firstNonBlank(span.getConversationId(), getAttributeValue(spanData, GEN_AI_CONVERSATION_ID)));
        span.setAgentId(getAttributeValue(spanData, GEN_AI_AGENT_ID));
    }

    private Map<String, Object> attributes(Attributes attributes) {
        Map<String, Object> map = new LinkedHashMap<>();
        attributes.forEach((key, value) -> map.put(key.getKey(), value));
        return map;
    }

    private void setTokens(Span span, SpanData spanData) {
        span.setInputTokens(getAttributeValue(spanData, GEN_AI_USAGE_INPUT_TOKENS));
        span.setOutputTokens(getAttributeValue(spanData, GEN_AI_USAGE_OUTPUT_TOKENS));
        span.setTotalTokens(totalTokens(span, spanData));
    }

    private Long totalTokens(Span span, SpanData spanData) {
        return sum(span.getInputTokens(), span.getOutputTokens());
    }

    private Long sum(Long left, Long right) {
        return left == null && right == null ? null : (left == null ? 0 : left) + (right == null ? 0 : right);
    }

    private <T> T getAttributeValue(SpanData spanData, AttributeKey<T> key) {
        T value = spanData.getAttributes().get(key);
        if (value != null) {
            return value;
        }
        return spanData.getResource().getAttribute(key);
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }

}
