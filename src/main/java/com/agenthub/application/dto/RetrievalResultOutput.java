package com.agenthub.application.dto;

/**
 * 检索结果输出DTO。
 */
public record RetrievalResultOutput(
        /** 文档ID */String documentId,
        /** 块ID */String chunkId,
        /** 内容 */String content,
        /** 分数 */double score
) {
}
