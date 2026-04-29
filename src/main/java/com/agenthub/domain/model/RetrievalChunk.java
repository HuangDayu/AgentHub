package com.agenthub.domain.model;

/**
 * 检索结果片段。从知识库检索到的文档内容片段。
 */
public record RetrievalChunk(
    /** 文档内容 */
    String content,
    /** 文档标题/来源 */
    String documentTitle,
    /** 文档 ID */
    String documentId,
    /** Chunk ID */
    String chunkId,
    /** 相似度分数 */
    double score,
    /** 知识库 ID */
    String knowledgeBaseId
) {}
