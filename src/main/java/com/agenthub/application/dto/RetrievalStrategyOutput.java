package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalStrategyOutput {
    private String id;
    private String name;
    private String description;
    private String retrievalType;
    private int topK;
    private double similarityThreshold;
    private boolean rerankEnabled;
    private String rerankModel;
    private double vectorWeight;
    private double keywordWeight;
    private Instant createdAt;
    private Instant updatedAt;
}
