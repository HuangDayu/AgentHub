package com.agenthub.application.command;

import java.util.List;

/**
 * 创建工具策略命令。
 */
public record CreateToolStrategyCommand(
    String workspaceId,
    String name,
    String description,
    int maxConcurrentCalls,
    int timeoutSeconds,
    int retryCount,
    boolean fallbackEnabled,
    List<String> allowedTools
) {
}
