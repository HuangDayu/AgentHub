package com.agenthub.application.dto;

/**
 * 引用输出DTO。
 */
public record CitationOutput(
        /** 索引 */int index,
        /** 文档ID */String documentId,
        /** 块ID */String chunkId,
        /** 摘录 */String excerpt
) {
}
