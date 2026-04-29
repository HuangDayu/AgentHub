package com.agenthub.api.dto;

import java.time.Instant;

/**
 * 文档响应DTO。
 */
public record DocumentResponse(
        String docId,
        String kbId,
        String fileName,
        String contentType,
        long size,
        String status,
        Instant createdAt
) {
}
