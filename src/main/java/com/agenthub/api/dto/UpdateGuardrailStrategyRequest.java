package com.agenthub.api.dto;

/**
 * 更新护栏策略请求体。
 */
public record UpdateGuardrailStrategyRequest(
    String name,
    String description
) {
}
