package com.agenthub.api.dto;

/**
 * 引用项DTO。
 */
public record CitationItem(
        int index,
        String documentId,
        String chunkId,
        String excerpt
) {
}
