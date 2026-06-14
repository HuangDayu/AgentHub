package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识库摘要，供Agent进行知识库选择决策。
 */
@Data
@NoArgsConstructor
public class KnowledgeBaseSummary {
    private String knowledgeBaseId;
    private String name;
    private String description;
    private int documentCount;
    private String lastUpdatedAt;
    private List<String> supportedQueryTypes;
}
