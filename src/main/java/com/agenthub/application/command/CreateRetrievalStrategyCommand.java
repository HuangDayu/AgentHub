package com.agenthub.application.command;

/**
 * 创建检索策略命令。
 */
public record CreateRetrievalStrategyCommand(
        String workspaceId,
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
        double keywordWeight
) {
}
