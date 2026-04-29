package com.agenthub.api.dto;

import java.util.List;

/**
 * 前端 Tenant Console 知识检索响应。
 * <p>
 * 与前端 RetrievalChunk 接口对齐：{docId, chunkIndex, content, score}
 */
public record FrontendRetrieveResponse(
        List<FrontendChunk> chunks
) {
    public record FrontendChunk(
            String docId,
            int chunkIndex,
            String content,
            double score
    ) {
    }
}
