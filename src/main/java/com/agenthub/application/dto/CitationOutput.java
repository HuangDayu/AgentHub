package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitationOutput {
    private /** 索引 */int index;
    private /** 文档ID */String documentId;
    private /** 块ID */String chunkId;
    private /** 摘录 */String excerpt;
}
