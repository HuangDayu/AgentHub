package com.agenthub.api.dto;

/**
 * 创建模型策略请求体。
 */
public record CreateModelStrategyRequest(
    String name,
    String description,
    Double temperature,
    Integer maxTokens,
    Double topP,
    Double frequencyPenalty,
    Double presencePenalty
) {
}
