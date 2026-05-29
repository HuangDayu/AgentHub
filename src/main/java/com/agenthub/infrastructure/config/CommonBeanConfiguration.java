package com.agenthub.infrastructure.config;

import com.agenthub.common.utils.TtlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;
import java.util.concurrent.ExecutorService;

/**
 * @author huangdayu
 */
@Configuration
public class CommonBeanConfiguration {

    @Bean("ttlExecutorService")
    public ExecutorService ttlExecutorService() {
        return TtlUtils.getTtlExecutorService();
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
