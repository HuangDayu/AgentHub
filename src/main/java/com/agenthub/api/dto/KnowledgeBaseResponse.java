package com.agenthub.api.dto;

import com.agenthub.domain.model.KnowledgeBase;

import java.time.Instant;

/**
 * 知识库响应DTO。
 */
public record KnowledgeBaseResponse(
        String id,
        String kbCode,
        String name,
        String description,
        /** 关联的租户向量数据库配置ID（可为空） */
        String vectorStoreConfigId,
        String embeddingModelConfigId,
        String chatModelConfigId,
        Instant createdAt,
        Instant updatedAt
) {
    public static KnowledgeBaseResponse from(KnowledgeBase knowledgeBase) {
        return new KnowledgeBaseResponse(
                knowledgeBase.id(),
                knowledgeBase.kbCode(),
                knowledgeBase.name(),
                knowledgeBase.description(),
                knowledgeBase.vectorStoreConfigId(),
                knowledgeBase.embeddingModelConfigId(),
                knowledgeBase.chatModelConfigId(),
                knowledgeBase.createdAt(),
                knowledgeBase.updatedAt()
        );
    }
}
