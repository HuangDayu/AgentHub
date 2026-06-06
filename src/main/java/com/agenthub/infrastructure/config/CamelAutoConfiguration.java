package com.agenthub.infrastructure.config;

import com.agenthub.infrastructure.camel.CamelDataSourceRuntime;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Camel 启动/关闭钩子
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CamelAutoConfiguration {
    private final CamelDataSourceRuntime runtime;

    @PreDestroy
    public void onShutdown() {
        log.info("shutting down all CamelContexts");
        runtime.shutdownAll();
    }
}
