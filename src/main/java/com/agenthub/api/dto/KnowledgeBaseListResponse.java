package com.agenthub.api.dto;

import com.agenthub.domain.model.KnowledgeBase;

import java.util.List;

/**
 * 知识库列表响应DTO。
 */
public record KnowledgeBaseListResponse(
        List<KnowledgeBaseResponse> items
) {
    public static KnowledgeBaseListResponse from(List<KnowledgeBase> knowledgeBases) {
        List<KnowledgeBaseResponse> items = knowledgeBases.stream()
                .map(KnowledgeBaseResponse::from)
                .toList();
        return new KnowledgeBaseListResponse(items);
    }
}
