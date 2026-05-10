package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestionJobOutput {
    private /** 任务ID */String jobId;
    private /** 知识库ID */String kbId;
    private /** 文件数量 */int fileCount;
    private /** 状态 */String status;
    private /** 创建时间 */Instant createdAt;
}
