package com.agenthub.api.dto;

/**
 * 创建护栏策略请求体。
 */
public record CreateGuardrailStrategyRequest(
    String name,
    String description,
    Boolean inputValidationEnabled,
    Boolean outputValidationEnabled,
    Boolean piiDetectionEnabled,
    Boolean piiMaskingEnabled,
    Boolean promptInjectionDetection,
    Integer maxInputLength,
    Integer maxOutputLength
) {
}
