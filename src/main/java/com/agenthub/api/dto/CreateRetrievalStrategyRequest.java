package com.agenthub.api.dto;

import java.util.List;

/**
 * 创建检索策略请求体。
 */
public record CreateRetrievalStrategyRequest(
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
