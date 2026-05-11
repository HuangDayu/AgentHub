package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 前端 Tenant Console 知识检索响应。
 * <p>
 * 与前端 RetrievalChunk 接口对齐：{docId, chunkIndex, content, score}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontendRetrieveResponse {
    private List<FrontendChunk> chunks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrontendChunk {
        private String docId;
        private int chunkIndex;
        private String content;
        private double score;
    }
}
