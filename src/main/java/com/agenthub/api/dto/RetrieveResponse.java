package com.agenthub.api.dto;

import java.util.List;

/**
 * 知识库检索响应体，包含检索结果和引用信息。
 */
public record RetrieveResponse(
        List<RetrievalResultItem> results,
        List<CitationItem> citations
) {
}
