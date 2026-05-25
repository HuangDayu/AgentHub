package com.agenthub.infrastructure.telemetry;

import com.agenthub.application.port.out.repositories.OtlpSpanRepository;
import com.agenthub.common.annotations.IgnoreTenantContext;
import com.agenthub.domain.model.telemetry.OtlpSpan;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;

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
    private final OtlpSpanRepository spanRepository;

    @IgnoreTenantContext
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {

        try {
            int count = 0;
            for (SpanData spanData : spans) {
                exportSpan(spanData);
                count++;
            }
            log.debug("Exported {} spans to database", count);
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
        OtlpSpan span = convertToOtlpSpan(spanData);
        spanRepository.save(span);
    }

    private OtlpSpan convertToOtlpSpan(SpanData spanData) {
        OtlpSpan span = new OtlpSpan();
        span.setSpanId(spanData.getSpanId());
        span.setTraceId(spanData.getTraceId());
        span.setParentSpanId(spanData.getParentSpanId());
        span.setOperationName(spanData.getName());
        span.setServiceName(extractServiceName(spanData));
        span.setKind(spanData.getKind().name());
        span.setStartTimestamp(spanData.getStartEpochNanos());
        span.setEndTimestamp(spanData.getEndEpochNanos());
        calculateDuration(span);
        span.setStatus(spanData.getStatus().getStatusCode().name());
        span.setStatusDescription(spanData.getStatus().getDescription());
        span.setAttributes(toJson(spanData.getAttributes()));
        span.setEvents(toJson(spanData.getEvents()));
        span.setLinks(toJson(spanData.getLinks()));
        span.setCreatedAt(Instant.now());
        return span;
    }

    private void calculateDuration(OtlpSpan span) {
        if (span.getStartTimestamp() != null && span.getEndTimestamp() != null) {
            span.setDuration(span.getEndTimestamp() - span.getStartTimestamp());
        }
    }

    private String extractServiceName(SpanData spanData) {
        return spanData.getResource()
                .getAttributes()
                .get(AttributeKey.stringKey("service.name"));
    }


}
