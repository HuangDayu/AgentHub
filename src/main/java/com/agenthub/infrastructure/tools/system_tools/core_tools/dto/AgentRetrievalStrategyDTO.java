package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import com.agenthub.domain.model.strategy.RetrievalStrategy.RetrievalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent检索策略DTO，仅暴露Agent决策所需的检索策略信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentRetrievalStrategyDTO {
    private String id;
    private String name;
    private String description;
    private RetrievalType retrievalType;
    private int topK;
    private double scoreThreshold;
    private boolean enableTranslationQuery;
    private boolean enableCompressionQuery;
    private boolean enableRerank;
    private boolean enableQueryRewrite;
    private boolean enableTextSearch;
    private boolean enableVectorSearch;
    private double vectorWeight;
    private double keywordWeight;
}
