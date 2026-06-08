package com.agenthub.infrastructure.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author huangdayu
 */
@Configuration
public class CommonBeanConfiguration {

    private ExecutorService ttlExecutor;

    @Bean("ttlExecutorService")
    public ExecutorService ttlExecutorService() {
        ttlExecutor = TtlExecutors.getTtlExecutorService(Executors.newVirtualThreadPerTaskExecutor());
        return ttlExecutor;
    }

    @PreDestroy
    public void shutdownTtlExecutor() {
        if (ttlExecutor == null) return;
        ttlExecutor.shutdown();
        awaitOrForceShutdown();
    }

    private void awaitOrForceShutdown() {
        try {
            if (!ttlExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                ttlExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ttlExecutor.shutdownNow();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

}
