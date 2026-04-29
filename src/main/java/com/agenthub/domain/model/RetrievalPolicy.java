package com.agenthub.domain.model;

import java.util.List;

/**
 * Agent 检索策略配置。
 * 定义 Agent 关联的知识库列表及检索参数。
 */
@Deprecated
public record RetrievalPolicy(
    /** 关联的知识库 ID 列表 */
    List<String> knowledgeBaseIds,
    /** 召回数量 */
    int topK,
    /** 相似度阈值 */
    double scoreThreshold,
    /** 是否启用查询重写 */
    boolean enableQueryRewrite,
    /** 是否启用 Rerank */
    boolean enableRerank
) {
    public RetrievalPolicy {
        if (knowledgeBaseIds == null) {
            knowledgeBaseIds = List.of();
        }
        if (topK <= 0) topK = 5;
        if (scoreThreshold <= 0) scoreThreshold = 0.7;
    }

    public static RetrievalPolicy empty() {
        return new RetrievalPolicy(List.of(), 5, 0.7, true, true);
    }

    public boolean hasKnowledgeBases() {
        return knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty();
    }
}
