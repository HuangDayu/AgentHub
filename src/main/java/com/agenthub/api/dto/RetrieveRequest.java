package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrieveRequest {
    private String query;
    private int topK;
    private double scoreThreshold;
    private boolean enableQueryRewrite;
    private boolean enableRerank;
    private boolean enableTextSearch;
    private boolean enableVectorSearch;
    private String rerankModel;
    private double vectorWeight;
    private double keywordWeight;
}
