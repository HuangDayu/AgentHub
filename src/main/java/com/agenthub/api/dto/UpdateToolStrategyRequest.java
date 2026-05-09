package com.agenthub.api.dto;

import java.util.List;

/**
 * 更新工具策略请求体。
 */
public record UpdateToolStrategyRequest(
    String name,
    String description,
    Integer maxConcurrentCalls,
    Integer timeoutSeconds,
    Integer retryCount,
    Boolean fallbackEnabled,
    List<String> allowedTools
) {
}
