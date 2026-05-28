package com.agenthub.infrastructure.agents.aliyun.knowledge;

import com.agenthub.application.port.out.etl.EtlDocumentChunkStorePort;
import com.agenthub.application.port.out.rag.RagVectorSearchPort;
import com.agenthub.domain.model.etl.DocumentChunk;
import com.agenthub.domain.model.rag.RetrievalResult;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.DocumentMetadata;
import io.agentscope.core.rag.model.RetrieveConfig;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
public class AgentScopeKnowledge implements Knowledge {


    private final RagVectorSearchPort ragVectorSearchPort;
    private final EtlDocumentChunkStorePort etlDocumentChunkStorePort;
    private final String kbId;

    @Override
    public Mono<Void> addDocuments(List<Document> documents) {
        List<DocumentChunk> chunks = documents.stream()
                .map(this::toDocumentChunk)
                .toList();
        etlDocumentChunkStorePort.saveAll(kbId, chunks);
        return Mono.empty();
    }

    private DocumentChunk toDocumentChunk(Document document) {
        float[] embedding = null;
        if (document.getEmbedding() != null && document.getEmbedding().length > 0) {
            embedding = new float[document.getEmbedding().length];
            for (int i = 0; i < document.getEmbedding().length; i++) {
                embedding[i] = Float.parseFloat(String.valueOf(document.getEmbedding()[i]));
            }
        }
        return new DocumentChunk(document.getMetadata().getChunkId(), document.getMetadata().getDocId(), kbId, 0, document.getMetadata().getContentText(), 0, embedding);
    }

    @Override
    public Mono<List<Document>> retrieve(String query, RetrieveConfig config) {
        List<RetrievalResult> search = ragVectorSearchPort.search(kbId, query, config.getLimit());
        List<Document> documents = search.stream()
                .map(this::toDocument)
                .toList();
        return Mono.just(documents);
    }

    private Document toDocument(RetrievalResult retrievalResult) {
        Document document = new Document(new DocumentMetadata(TextBlock.builder().text(retrievalResult.getContent()).build(),
                retrievalResult.getDocumentId(), retrievalResult.getChunkId(), retrievalResult.getMetadata()));
        document.setScore(retrievalResult.getScore());
        document.setVectorName(retrievalResult.getDocumentTitle());
        return document;
    }
}
