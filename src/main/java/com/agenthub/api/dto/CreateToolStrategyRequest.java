package com.agenthub.api.dto;

import java.util.List;

/**
 * 创建工具策略请求体。
 */
public record CreateToolStrategyRequest(
    String name,
    String description,
    Integer maxConcurrentCalls,
    Integer timeoutSeconds,
    Integer retryCount,
    Boolean fallbackEnabled,
    List<String> allowedTools
) {
}
