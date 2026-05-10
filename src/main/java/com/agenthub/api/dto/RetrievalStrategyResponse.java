package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalStrategyResponse {
    private String id;
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
    private Instant createdAt;
    private Instant updatedAt;
}
