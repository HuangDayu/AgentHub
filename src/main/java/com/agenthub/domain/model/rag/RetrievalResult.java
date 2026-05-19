package com.agenthub.domain.model.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalResult {
    /**
     * 所属文档ID
     */
    private String documentId;
    /**
     * 文档标题
     */
    private String documentTitle;
    /**
     * 所属分块ID
     */
    private String chunkId;
    /**
     * 分块内容
     */
    private String content;
    /**
     * 相关性分数（0-1之间）
     */
    private double score;

    private Map<String, Object> metadata;
}
