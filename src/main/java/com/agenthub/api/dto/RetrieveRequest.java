package com.agenthub.api.dto;

/**
 * 知识库检索请求体。
 */
public record RetrieveRequest(
        String query,
        int topK,
        double scoreThreshold,
        boolean enableQueryRewrite,
        boolean enableRerank,
        boolean enableTextSearch,
        boolean enableVectorSearch,
        String rerankModel,
        double vectorWeight,
        double keywordWeight
) {
}
