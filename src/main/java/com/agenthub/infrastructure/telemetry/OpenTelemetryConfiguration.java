package com.agenthub.infrastructure.telemetry;

import io.agentscope.core.Version;
import io.agentscope.core.tracing.TracerRegistry;
import io.agentscope.core.tracing.telemetry.TelemetryTracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry配置
 * <p>
 * 设计说明：
 * 1. TelemetryTracer仍然使用endpoint方式（这是agentscope库的要求）
 * 2. 但endpoint指向本应用的OTLP接收端点
 * 3. 数据通过Controller接收后，直接存储到数据库
 * 4. 同时提供TelemetryDataCollector，可在应用内部直接收集数据
 * <p>
 * 两种使用方式：
 * - 方式1：通过TelemetryTracer自动追踪（需要HTTP调用）
 * - 方式2：通过TelemetryDataCollector直接收集（无需HTTP调用）
 */
@RequiredArgsConstructor
@Configuration
public class OpenTelemetryConfiguration {

    private final DatabaseSpanExporter databaseSpanExporter;

    @PostConstruct
    public void init() {
        TracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(databaseSpanExporter).build())
                .setSampler(Sampler.alwaysOn())
                .build();
        TracerRegistry.register(TelemetryTracer.builder()
                .tracer(tracerProvider.get("agentscope-java", Version.VERSION))
                .build());
    }
}
