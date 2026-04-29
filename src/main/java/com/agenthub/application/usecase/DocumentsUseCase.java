package com.agenthub.application.usecase;

import com.agenthub.application.port.out.rag.ChunkStorePort;
import com.agenthub.application.port.out.rag.DocumentStoragePort;
import com.agenthub.application.port.out.repositories.IngestionDocumentChunkRepository;
import com.agenthub.application.port.out.repositories.IngestionDocumentRepository;
import com.agenthub.domain.model.DocumentChunk;
import com.agenthub.domain.model.IngestionDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文档管理用例。
 */
@Service
public class DocumentsUseCase {

    private final IngestionDocumentRepository documentRepository;
    private final IngestionDocumentChunkRepository ingestionDocumentChunkRepository;
    private final ChunkStorePort chunkStorePort;
    private final DocumentStoragePort documentStoragePort;

    public DocumentsUseCase(IngestionDocumentRepository documentRepository, IngestionDocumentChunkRepository ingestionDocumentChunkRepository, ChunkStorePort chunkStorePort, DocumentStoragePort documentStoragePort) {
        this.documentRepository = documentRepository;
        this.ingestionDocumentChunkRepository = ingestionDocumentChunkRepository;
        this.chunkStorePort = chunkStorePort;
        this.documentStoragePort = documentStoragePort;
    }

    public List<IngestionDocument> listByKbId(String kbId) {
        return documentRepository.findByKbId(kbId);
    }

    @Transactional
    public void deleteById(String kbId, String docId) {
        IngestionDocument document = documentRepository.findByDocId(docId);
        List<DocumentChunk> allChunks = ingestionDocumentChunkRepository.findList(kbId, docId);
        chunkStorePort.deleteAll(kbId, allChunks.stream().map(DocumentChunk::getChunkId).toList());
        ingestionDocumentChunkRepository.deleteAll(kbId, docId);
        documentStoragePort.delete(document.getStoragePath());
        documentRepository.deleteById(docId);
    }
}
