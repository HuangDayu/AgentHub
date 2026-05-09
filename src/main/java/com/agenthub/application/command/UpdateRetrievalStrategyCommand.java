package com.agenthub.application.command;

/**
 * 更新检索策略命令。
 *
 * @param name        策略名称
 * @param description 策略描述
 */
public record UpdateRetrievalStrategyCommand(
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
