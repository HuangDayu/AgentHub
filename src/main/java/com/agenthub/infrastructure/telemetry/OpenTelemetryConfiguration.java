package com.agenthub.infrastructure.telemetry;

import io.agentscope.core.Version;
import io.agentscope.core.studio.StudioManager;
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
 * 1. AgentScope 通过 TelemetryTracer 产生追踪数据
 * 2. BatchSpanProcessor 将 SpanData 交给数据库导出器
 * 3. 数据库导出器写入统一的 spans 表供数据视图查询
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
