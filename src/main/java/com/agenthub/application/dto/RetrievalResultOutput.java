package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalResultOutput {
    /**
     * 文档ID
     */
    private String documentId;
    /**
     * 块ID
     */
    private String chunkId;
    /**
     * 内容
     */
    private String content;
    /**
     * 分数
     */
    private double score;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}
