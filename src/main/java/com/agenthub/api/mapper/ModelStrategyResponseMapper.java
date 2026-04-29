package com.agenthub.api.mapper;

import com.agenthub.api.dto.ModelStrategyResponse;
import com.agenthub.domain.model.ModelStrategy;

/**
 * 模型策略响应映射器。
 */
public final class ModelStrategyResponseMapper {

    private ModelStrategyResponseMapper() {
    }

    public static ModelStrategyResponse toResponse(ModelStrategy strategy) {
        return new ModelStrategyResponse(
            strategy.getId(),
            strategy.getName(),
            strategy.getDescription(),
            strategy.getTemperature(),
            strategy.getMaxTokens(),
            strategy.getTopP(),
            strategy.getFrequencyPenalty(),
            strategy.getPresencePenalty(),
            strategy.getCreatedAt(),
            strategy.getUpdatedAt()
        );
    }
}
