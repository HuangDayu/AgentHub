package com.agenthub.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * 入库任务响应DTO。
 */
public record IngestionJobResponse(
        String jobId,
        String kbId,
        String status,
        int documentCount,
        Instant createdAt,
        List<IngestionDocumentResponse> documents
) {
}