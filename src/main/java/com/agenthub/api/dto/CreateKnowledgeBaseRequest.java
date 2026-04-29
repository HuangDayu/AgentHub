package com.agenthub.api.dto;

import java.util.List;

public record CreateKnowledgeBaseRequest(
        String tenantId,
        String workspaceId,
        String kbCode,
        String name,
        String description,
        String indexProvider,
        List<String> indexVersions,
        String activeIndexVersion,
        String vectorStoreConfigId,
        String embeddingModelConfigId,
        String chatModelConfigId,
        String retrievalPolicy
) {
}
