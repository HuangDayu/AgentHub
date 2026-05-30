package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库推荐结果，根据查询主题推荐最相关的知识库。
 */
@Data
@NoArgsConstructor
public class KnowledgeBaseRecommendation {
    private String knowledgeBaseId;
    private String name;
    private String reason;
    private String relevanceScore;
}
