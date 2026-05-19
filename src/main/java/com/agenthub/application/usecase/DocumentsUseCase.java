package com.agenthub.application.usecase;

import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.etl.ExtractTransformLoadPort;
import com.agenthub.application.port.out.repositories.IngestionDocumentChunkRepository;
import com.agenthub.application.port.out.repositories.IngestionDocumentRepository;
import com.agenthub.domain.model.etl.DocumentChunk;
import com.agenthub.domain.model.etl.IngestionDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文档管理用例。
 */
@Component
@RequiredArgsConstructor
public class DocumentsUseCase {

    private final IngestionDocumentRepository documentRepository;
    private final IngestionDocumentChunkRepository ingestionDocumentChunkRepository;
    private final ExtractTransformLoadPort extractTransformLoadPort;
    private final DocumentFileStoragePort documentFileStoragePort;

    public List<IngestionDocument> listByKbId(String kbId) {
        return documentRepository.findByKbId(kbId);
    }

    @Transactional
    public void deleteById(String kbId, String docId) {
        IngestionDocument document = documentRepository.findByDocId(docId);
        List<DocumentChunk> allChunks = ingestionDocumentChunkRepository.findList(kbId, docId);
        extractTransformLoadPort.delete(allChunks);
        ingestionDocumentChunkRepository.deleteAll(kbId, docId);
        documentFileStoragePort.delete(document.getStoragePath());
        documentRepository.deleteById(docId);
    }
}
