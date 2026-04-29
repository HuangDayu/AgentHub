package com.agenthub.application.command;

/**
 * 更新工具策略命令。
 *
 * @param name        策略名称
 * @param description 策略描述
 */
public record UpdateToolStrategyCommand(
    String name,
    String description
) {
}
