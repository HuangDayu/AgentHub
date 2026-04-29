package com.agenthub.api.dto;

/**
 * 前端 Tenant Console 知识检索请求。
 * <p>
 * kbCode 通过 body 传递（而非路径变量），与前端 API 契约一致。
 */
public record FrontendRetrieveRequest(
        String kbId,
        String query,
        Integer topK
) {
    public FrontendRetrieveRequest {
        if (topK == null) {
            topK = 5;
        }
    }
}
