package com.agenthub.application.command;

/**
 * @author huangdayu
 */
public record RetrievalCommand(
        String kbId,
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