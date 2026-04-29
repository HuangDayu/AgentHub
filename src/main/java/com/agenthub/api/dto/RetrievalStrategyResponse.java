package com.agenthub.api.dto;

import java.time.Instant;

/**
 * 检索策略响应体。
 */
public record RetrievalStrategyResponse(
    String id,
    String name,
    String description,
    String retrievalType,
    int topK,
    double scoreThreshold,
    boolean enableRerank,
    boolean enableQueryRewrite,
    boolean enableTextSearch,
    boolean enableVectorSearch,
    String rerankModel,
    double vectorWeight,
    double keywordWeight,
    Instant createdAt,
    Instant updatedAt
) {
}
