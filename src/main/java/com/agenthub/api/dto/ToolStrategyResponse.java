package com.agenthub.api.dto;

import java.time.Instant;

/**
 * 工具策略响应体。
 */
public record ToolStrategyResponse(
    String id,
    String name,
    String description,
    int maxConcurrentCalls,
    int timeoutSeconds,
    int retryCount,
    boolean fallbackEnabled,
    Instant createdAt,
    Instant updatedAt
) {
}
