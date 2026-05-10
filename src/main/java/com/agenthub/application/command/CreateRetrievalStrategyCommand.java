package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRetrievalStrategyCommand {
    private String workspaceId;
    private String name;
    private String description;
    private String retrievalType;
    private int topK;
    private double scoreThreshold;
    private boolean enableRerank;
    private boolean enableQueryRewrite;
    private boolean enableTextSearch;
    private boolean enableVectorSearch;
    private String rerankModel;
    private double vectorWeight;
    private double keywordWeight;
}
