package com.agenthub.api.dto;

import java.time.Instant;

/**
 * 护栏策略响应体。
 */
public record GuardrailStrategyResponse(
    String id,
    String name,
    String description,
    boolean inputValidationEnabled,
    boolean outputValidationEnabled,
    boolean piiDetectionEnabled,
    boolean piiMaskingEnabled,
    boolean promptInjectionDetection,
    int maxInputLength,
    int maxOutputLength,
    Instant createdAt,
    Instant updatedAt
) {
}
