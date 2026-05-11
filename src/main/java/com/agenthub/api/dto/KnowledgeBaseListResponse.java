package com.agenthub.api.dto;

import com.agenthub.domain.model.KnowledgeBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseListResponse {
    private List<KnowledgeBaseResponse> items;

    public static KnowledgeBaseListResponse from(List<KnowledgeBase> knowledgeBases) {
        List<KnowledgeBaseResponse> items = knowledgeBases.stream()
                .map(KnowledgeBaseResponse::from)
                .toList();
        return new KnowledgeBaseListResponse(items);
    }
}