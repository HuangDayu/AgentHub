package com.agenthub.application.command;

/**
 * 更新护栏策略命令。
 *
 * @param name        策略名称
 * @param description 策略描述
 */
public record UpdateGuardrailStrategyCommand(
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
