package com.agenthub.api.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateKnowledgeBaseRequest {
    private String tenantId;
    private String workspaceId;
    private String kbCode;
    private String name;
    private String description;
    private String indexProvider;
    private List<String> indexVersions;
    private String activeIndexVersion;
    private String vectorStoreConfigId;
    private String embeddingModelConfigId;
    private String chatModelConfigId;
    private String retrievalPolicy;
}
