package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRetrievalStrategyCommand {
    private String workspaceId;
    private String name;
    private String description;
    private String retrievalType;
    private Integer topK;
    private Double scoreThreshold;
    private Boolean enableRerank;
    private Boolean enableQueryRewrite;
    private Boolean enableTextSearch;
    private Boolean enableVectorSearch;
    private String rerankModel;
    private Double vectorWeight;
    private Double keywordWeight;
}
