package com.agenthub.application.command;

/**
 * 创建模型策略命令。
 */
public record CreateModelStrategyCommand(
    String workspaceId,
    String name,
    String description,
    double temperature,
    int maxTokens,
    double topP,
    double frequencyPenalty,
    double presencePenalty
) {
}
