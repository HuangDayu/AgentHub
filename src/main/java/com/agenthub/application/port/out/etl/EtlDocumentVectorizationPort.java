package com.agenthub.application.port.out.etl;

import com.agenthub.domain.model.etl.DocumentChunk;

import java.util.List;

/**
 * 向量化端口接口。
 */
public interface EtlDocumentVectorizationPort {

    /**
     * 对文档分块进行向量化，生成嵌入向量。
     *
     * @param chunks 待向量化的文档分块列表
     * @return 包含嵌入向量的文档分块列表
     */
    List<DocumentChunk> vectorize(String embeddingModelConfigId, List<DocumentChunk> chunks);
}