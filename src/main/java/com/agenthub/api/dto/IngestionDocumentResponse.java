package com.agenthub.api.dto;

/**
 * 入库文档响应DTO。
 */
public record IngestionDocumentResponse(
        String documentId,
        String fileName,
        String contentType,
        long size
) {
}