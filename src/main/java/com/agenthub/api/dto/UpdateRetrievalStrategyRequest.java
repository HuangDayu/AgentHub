package com.agenthub.api.dto;

/**
 * 更新检索策略请求体。
 */
public record UpdateRetrievalStrategyRequest(
    String name,
    String description
) {
}
