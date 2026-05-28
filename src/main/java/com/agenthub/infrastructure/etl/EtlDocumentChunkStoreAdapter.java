package com.agenthub.infrastructure.etl;

import com.agenthub.application.port.out.etl.EtlDocumentChunkStorePort;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.etl.DocumentChunk;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Spring AI Qdrant 分块仓储实现。
 */
@Component
public class EtlDocumentChunkStoreAdapter implements EtlDocumentChunkStorePort {
    private static final Logger log = LoggerFactory.getLogger(EtlDocumentChunkStoreAdapter.class);
    private final SpringShareObjectFactory springShareObjectFactory;

    public EtlDocumentChunkStoreAdapter(SpringShareObjectFactory springShareObjectFactory) {
        this.springShareObjectFactory = springShareObjectFactory;
    }

    /**
     * 将文档分块列表保存到Qdrant向量存储。
     *
     * @param chunks 待保存的文档分块列表
     */
    @Override
    public void saveAll(String kbId, List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return;
        VectorStore vectorStore = getVectorStore(kbId);
        List<Document> docs = convertToDocuments(chunks);
        vectorStore.add(docs);
        log.info("Saved {} chunks to Spring AI VectorStore", docs.size());
    }

    @Override
    public void deleteAll(String kbId, List<String> documentChunkIds) {
        if (documentChunkIds == null || documentChunkIds.isEmpty()) return;
        VectorStore vectorStore = springShareObjectFactory.getVectorStoreByKbId(kbId);
        if (vectorStore == null) {
            throw new NotFoundException("VectorStore not found");
        }
        vectorStore.delete(documentChunkIds);
        log.info("Deleted {} chunks from Spring AI VectorStore", documentChunkIds.size());
    }

    /**
     * 获取向量存储。
     */
    private VectorStore getVectorStore(String kbId) {
        VectorStore vectorStore = springShareObjectFactory.getVectorStoreByKbId(kbId);
        if (vectorStore == null) {
            throw new NotFoundException("VectorStore not found");
        }
        return vectorStore;
    }

    /**
     * 转换文档块为Spring AI文档。
     */
    private List<Document> convertToDocuments(List<DocumentChunk> chunks) {
        return chunks.stream().map(c ->
                new Document(c.getChunkId(), c.getContent(), Map.of(
                        "document_id", c.getDocumentId(),
                        "kb_id", c.getKbId(),
                        "chunk_index", String.valueOf(c.getChunkIndex())
                ))
        ).toList();
    }
}
