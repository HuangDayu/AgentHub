package com.agenthub.api.dto;

import java.time.Instant;

/**
 * 模型策略响应体。
 */
public record ModelStrategyResponse(
    String id,
    String name,
    String description,
    double temperature,
    int maxTokens,
    double topP,
    double frequencyPenalty,
    double presencePenalty,
    Instant createdAt,
    Instant updatedAt
) {
}
