package com.agenthub.api.dto;

/**
 * 检索结果项DTO。
 */
public record RetrievalResultItem(
        String documentId,
        String chunkId,
        String content,
        double score
) {
}
