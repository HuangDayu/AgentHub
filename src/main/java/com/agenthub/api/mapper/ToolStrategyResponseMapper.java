package com.agenthub.api.mapper;

import com.agenthub.api.dto.ToolStrategyResponse;
import com.agenthub.domain.model.ToolStrategy;

/**
 * 工具策略响应映射器。
 */
public final class ToolStrategyResponseMapper {

    private ToolStrategyResponseMapper() {
    }

    public static ToolStrategyResponse toResponse(ToolStrategy strategy) {
        return new ToolStrategyResponse(
            strategy.getId(),
            strategy.getName(),
            strategy.getDescription(),
            strategy.getMaxConcurrentCalls(),
            strategy.getTimeoutSeconds(),
            strategy.getRetryCount(),
            strategy.isFallbackEnabled(),
            strategy.getCreatedAt(),
            strategy.getUpdatedAt()
        );
    }
}
