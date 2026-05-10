package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalResultOutput {
    private /** 文档ID */String documentId;
    private /** 块ID */String chunkId;
    private /** 内容 */String content;
    private /** 分数 */double score;
}
