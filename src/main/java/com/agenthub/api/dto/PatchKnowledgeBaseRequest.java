package com.agenthub.api.dto;

import java.util.List;

/**
 * 更新知识库请求DTO。
 */
public record PatchKnowledgeBaseRequest(
        String kbId,
        String kbCode,
        String name,
        String description,
        String vectorStoreConfigId,
        String embeddingModelConfigId,
        String chatModelConfigId
) {
}
