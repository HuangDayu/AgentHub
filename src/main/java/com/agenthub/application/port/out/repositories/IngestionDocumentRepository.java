package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.IngestionDocument;

import java.util.List;

/**
 * 入库文档仓储接口。
 */
public interface IngestionDocumentRepository {

    /**
     * 批量保存入库文档。
     *
     * @param documents 待保存的文档列表
     */
    void saveAll(List<IngestionDocument> documents);

    /**
     * 根据知识库ID查询文档列表。
     *
     * @param kbId 知识库ID
     * @return 该知识库下的文档列表
     */
    List<IngestionDocument> findByKbId(String kbId);

    IngestionDocument save(IngestionDocument documents);

    /**
     * 根据任务ID查询文档列表。
     *
     * @param jobId 入库任务ID
     * @return 该任务下的文档列表
     */
    List<IngestionDocument> findByJobId(String jobId);

    /**
     * 根据文档ID删除文档。
     *
     * @param documentId 文档ID
     */
    void deleteById(String documentId);

    /**
     * 批量更新文档。
     *
     * @param documents
     */
    void updateAll(List<IngestionDocument> documents);

    IngestionDocument findByDocId(String docId);
}