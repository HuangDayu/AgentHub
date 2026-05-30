package com.agenthub.infrastructure.tools.mcp_tools.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * MCP 平台客户端配置。
 */
@Configuration
public class McpPlatformConfig {

    @Bean("smitheryPlatformClient")
    public McpPlatformClient smitheryPlatformClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new SmitheryPlatformClient(restTemplate, objectMapper);
    }

    @Bean("glamaPlatformClient")
    public McpPlatformClient glamaPlatformClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new GlamaPlatformClient(restTemplate, objectMapper);
    }
}
