package com.agenthub.application.port.out.etl;

import com.agenthub.domain.model.etl.DocumentChunk;

import java.util.List;

/**
 * 文档分块端口接口。
 */
public interface EtlDocumentChunkerPort {

    /**
     * 将文档内容切分为多个分块。
     *
     * @param request 分块请求
     * @return 文档分块列表
     */
    List<DocumentChunk> chunk(ChunkSpec spec);
}