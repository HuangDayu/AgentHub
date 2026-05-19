package com.agenthub.api.mapper;

import com.agenthub.api.dto.RetrievalStrategyResponse;
import com.agenthub.domain.model.strategy.RetrievalStrategy;

/**
 * 检索策略响应映射器。
 */
public final class RetrievalStrategyResponseMapper {

    private RetrievalStrategyResponseMapper() {
    }

    public static RetrievalStrategyResponse toResponse(RetrievalStrategy strategy) {
        return new RetrievalStrategyResponse(
            strategy.getId(),
            strategy.getName(),
            strategy.getDescription(),
            strategy.getRetrievalType().name(),
            strategy.getTopK(),
            strategy.getScoreThreshold(),
            strategy.isEnableRerank(),
            strategy.isEnableQueryRewrite(),
            strategy.isEnableTextSearch(),
            strategy.isEnableVectorSearch(),
            strategy.getRerankModel(),
            strategy.getVectorWeight(),
            strategy.getKeywordWeight(),
            strategy.getCreatedAt(),
            strategy.getUpdatedAt()
        );
    }
}
