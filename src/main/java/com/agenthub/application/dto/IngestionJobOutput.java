package com.agenthub.application.dto;

import java.time.Instant;

/**
 * 入库任务输出DTO。
 */
public record IngestionJobOutput(
        /** 任务ID */String jobId,
        /** 知识库ID */String kbId,
        /** 文件数量 */int fileCount,
        /** 状态 */String status,
        /** 创建时间 */Instant createdAt
) {
}
