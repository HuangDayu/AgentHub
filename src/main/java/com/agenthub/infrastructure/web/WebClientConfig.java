package com.agenthub.infrastructure.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 配置。
 *
 * @author huangdayu
 */
@Configuration
public class WebClientConfig {

    /**
     * 提供 WebClient.Builder bean。
     *
     * @return WebClient.Builder 实例
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
