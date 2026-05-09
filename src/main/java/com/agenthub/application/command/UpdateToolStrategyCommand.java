package com.agenthub.application.command;

import java.util.List;

/**
 * 更新工具策略命令。
 *
 * @param name        策略名称
 * @param description 策略描述
 */
public record UpdateToolStrategyCommand(
    String workspaceId,
    String name,
    String description,
    Integer maxConcurrentCalls,
    Integer timeoutSeconds,
    Integer retryCount,
    Boolean fallbackEnabled,
    List<String> allowedTools
) {
}
