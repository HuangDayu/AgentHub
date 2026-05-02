package com.agenthub.infrastructure.etl;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.port.out.etl.EtlDocumentChunkStorePort;
import com.agenthub.domain.model.DocumentChunk;
import com.agenthub.infrastructure.spring.SpringAiObjectPoolManager;
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
    private final SpringAiObjectPoolManager springAiObjectPoolManager;

    public EtlDocumentChunkStoreAdapter(SpringAiObjectPoolManager springAiObjectPoolManager) {
        this.springAiObjectPoolManager = springAiObjectPoolManager;
    }

    /**
     * 将文档分块列表保存到Qdrant向量存储。
     *
     * @param chunks 待保存的文档分块列表
     */
    @Override
    public void saveAll(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return;
        VectorStore vectorStore = getVectorStore(chunks);
        List<Document> docs = convertToDocuments(chunks);
        vectorStore.add(docs);
        log.info("Saved {} chunks to Spring AI VectorStore", docs.size());
    }

    @Override
    public void deleteAll(String kbId, List<String> documentChunkIds) {
        if (documentChunkIds == null || documentChunkIds.isEmpty()) return;
        VectorStore vectorStore = springAiObjectPoolManager.getVectorStoreByKbId(kbId);
        if (vectorStore == null) {
            throw new NotFoundException("VectorStore not found");
        }
        vectorStore.delete(documentChunkIds);
        log.info("Deleted {} chunks from Spring AI VectorStore", documentChunkIds.size());
    }

    /**
     * 获取向量存储。
     */
    private VectorStore getVectorStore(List<DocumentChunk> chunks) {
        VectorStore vectorStore = springAiObjectPoolManager.getVectorStoreByKbId(chunks.getFirst().getKbId());
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
