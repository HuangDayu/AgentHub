package com.agenthub.application.dto;

import java.time.Instant;

/**
 * 检索策略输出DTO。
 */
public record RetrievalStrategyOutput(
    String id,
    String name,
    String description,
    String retrievalType,
    int topK,
    double similarityThreshold,
    boolean rerankEnabled,
    String rerankModel,
    double vectorWeight,
    double keywordWeight,
    Instant createdAt,
    Instant updatedAt
) {
}
