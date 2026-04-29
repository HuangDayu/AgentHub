package com.agenthub.application.command;

/**
 * 创建护栏策略命令。
 */
public record CreateGuardrailStrategyCommand(
    String workspaceId,
    String name,
    String description,
    boolean inputValidationEnabled,
    boolean outputValidationEnabled,
    boolean piiDetectionEnabled,
    boolean piiMaskingEnabled,
    boolean promptInjectionDetection,
    int maxInputLength,
    int maxOutputLength
) {
}
