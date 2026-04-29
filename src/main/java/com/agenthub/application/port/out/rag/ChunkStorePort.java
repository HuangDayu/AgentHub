package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.DocumentChunk;

import java.util.List;

/**
 * 分块仓储接口。
 */
public interface ChunkStorePort {

    /**
     * 批量保存文档分块。
     *
     * @param chunks 待保存的文档分块列表
     */
    void saveAll(List<DocumentChunk> chunks);

    void deleteAll(String kbId, List<String> documentChunkIds);
}