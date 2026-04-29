package com.agenthub.api.dto;

/**
 * 更新模型策略请求体。
 */
public record UpdateModelStrategyRequest(
        String name,
        String description,
        Double temperature,
        Integer maxTokens,
        Double topP,
        Double frequencyPenalty,
        Double presencePenalty
) {
}
