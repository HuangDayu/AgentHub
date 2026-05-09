package com.agenthub.api.dto;

/**
 * 更新护栏策略请求体。
 */
public record UpdateGuardrailStrategyRequest(
        String workspaceId,
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
