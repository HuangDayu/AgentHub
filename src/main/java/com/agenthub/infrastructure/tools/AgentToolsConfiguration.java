package com.agenthub.infrastructure.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * @author huangdayu
 */
@Configuration
public class AgentToolsConfiguration {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 30_000;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_INTERVAL_MS = 1_000;

    /**
     * 创建配置了连接超时和读取超时的 RestTemplate。
     * <p>
     * 实际调用时，单次请求的超时由 tool_registry.timeout_ms 动态覆盖。
     *
     * @return RestTemplate 实例
     */
    @Bean("tool.restTemplate")
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(DEFAULT_READ_TIMEOUT_MS);
        return new RestTemplate(factory);
    }

    /**
     * 创建重试模板，支持固定退避策略。
     * <p>
     * 对 {@link java.io.IOException} 和 HTTP 5xx 错误进行重试，
     * HTTP 4xx 客户端错误不重试。
     *
     * @return RetryTemplate 实例
     */
    @Bean("tool.retryTemplate")
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        // 重试策略：最多重试 3 次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(DEFAULT_MAX_RETRIES);
        retryTemplate.setRetryPolicy(retryPolicy);
        // 退避策略：每次重试间隔 1 秒
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(DEFAULT_RETRY_INTERVAL_MS);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }


}
