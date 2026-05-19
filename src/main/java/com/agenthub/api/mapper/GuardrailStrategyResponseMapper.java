package com.agenthub.api.mapper;

import com.agenthub.api.dto.GuardrailStrategyResponse;
import com.agenthub.domain.model.strategy.GuardrailStrategy;

/**
 * 护栏策略响应映射器。
 */
public final class GuardrailStrategyResponseMapper {

    private GuardrailStrategyResponseMapper() {
    }

    public static GuardrailStrategyResponse toResponse(GuardrailStrategy strategy) {
        return new GuardrailStrategyResponse(
            strategy.getId(),
            strategy.getName(),
            strategy.getDescription(),
            strategy.isInputValidationEnabled(),
            strategy.isOutputValidationEnabled(),
            strategy.isPiiDetectionEnabled(),
            strategy.isPiiMaskingEnabled(),
            strategy.isPromptInjectionDetection(),
            strategy.getMaxInputLength(),
            strategy.getMaxOutputLength(),
            strategy.getCreatedAt(),
            strategy.getUpdatedAt()
        );
    }
}
