package com.agenthub.api.dto;

/**
 * 更新检索策略请求体。
 */
public record UpdateRetrievalStrategyRequest(
        String workspaceId,
        String name,
        String description,
        String retrievalType,
        Integer topK,
        Double scoreThreshold,
        Boolean enableRerank,
        Boolean enableQueryRewrite,
        Boolean enableTextSearch,
        Boolean enableVectorSearch,
        String rerankModel,
        Double vectorWeight,
        Double keywordWeight
) {
}
