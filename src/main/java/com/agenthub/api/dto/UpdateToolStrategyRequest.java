package com.agenthub.api.dto;

/**
 * 更新工具策略请求体。
 */
public record UpdateToolStrategyRequest(
    String name,
    String description
) {
}
