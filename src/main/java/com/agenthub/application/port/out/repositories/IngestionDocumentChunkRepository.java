package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.etl.DocumentChunk;

import java.util.List;

/**
 * @author huangdayu
 */
public interface IngestionDocumentChunkRepository {

    void saveAll(List<DocumentChunk> allChunks);

    List<DocumentChunk> findList(String kbId, String docId);

    void deleteAll(String kbId, String docId);
}
