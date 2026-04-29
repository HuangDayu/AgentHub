package com.agenthub.api.dto;

/**
 * 创建入库任务响应DTO。
 */
public record CreateIngestionJobResponse(
        String jobId,
        String kbId,
        String status,
        int documentCount
) {
}