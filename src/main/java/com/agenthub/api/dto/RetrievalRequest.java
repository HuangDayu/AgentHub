package com.agenthub.api.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalRequest {
    private String tenantId;
    private String workspaceId;
    private List<String> knowledgeBaseIds;
    private String query;
    private int topK;
    private double scoreThreshold;
    private boolean enableQueryRewrite;
    private boolean enableRerank;
}
