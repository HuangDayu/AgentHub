package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.DocumentChunk;

import java.util.List;

/**
 * 文档分块端口接口。
 */
public interface DocumentChunkerPort {

    /**
     * 将文档内容切分为多个分块。
     *
     * @param documentId 文档ID
     * @param kbId       知识库ID
     * @param content    文档内容
     * @param chunkSize  分块大小（字符数）
     * @param overlap    分块重叠大小（字符数）
     * @return 文档分块列表
     */
    List<DocumentChunk> chunk(String documentId, String kbId, String content, int chunkSize, int overlap);
}