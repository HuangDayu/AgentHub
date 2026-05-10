package com.agenthub.api.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatchKnowledgeBaseRequest {
    private String kbId;
    private String kbCode;
    private String name;
    private String description;
    private String vectorStoreConfigId;
    private String embeddingModelConfigId;
    private String chatModelConfigId;
}
