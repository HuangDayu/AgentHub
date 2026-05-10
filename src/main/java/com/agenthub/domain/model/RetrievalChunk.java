package com.agenthub.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalChunk {
    private /** 文档内容 */ String content;
    private /** 文档标题/来源 */ String documentTitle;
    private /** 文档 ID */ String documentId;
    private /** Chunk ID */ String chunkId;
    private /** 相似度分数 */ double score;
    private /** 知识库 ID */ String knowledgeBaseId;
}
