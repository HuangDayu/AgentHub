package com.agenthub.application.command;

/**
 * 更新模型策略命令。
 *
 * @param name        策略名称
 * @param description 策略描述
 */
public record UpdateModelStrategyCommand(
        String id,
        String name,
        String description,
        Double temperature,
        Integer maxTokens,
        Double topP,
        Double frequencyPenalty,
        Double presencePenalty
) {
}
