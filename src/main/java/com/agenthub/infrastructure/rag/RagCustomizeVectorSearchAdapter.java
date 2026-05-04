package com.agenthub.infrastructure.rag;

import com.agenthub.application.port.out.rag.RagVectorSearchPort;
import com.agenthub.domain.model.RetrievalResult;
import com.agenthub.infrastructure.pool.SpringAiObjectPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Spring AI Qdrant 向量搜索适配器。
 * <p>
 * 当 VectorStore 不可用时，返回空结果并记录日志。
 */
@Component
public class RagCustomizeVectorSearchAdapter implements RagVectorSearchPort {
    private static final Logger log = LoggerFactory.getLogger(RagCustomizeVectorSearchAdapter.class);
    private final SpringAiObjectPoolManager springAiObjectPoolManager;

    /**
     * 构造适配器。
     *
     * @param springAiObjectPoolManager Spring AI 框架适配器
     */
    public RagCustomizeVectorSearchAdapter(SpringAiObjectPoolManager springAiObjectPoolManager) {
        this.springAiObjectPoolManager = springAiObjectPoolManager;
    }

    /**
     * 搜索向量。
     *
     * @param kbId      知识库 ID
     * @param queryText 查询文本
     * @param topK      返回数量
     * @return 检索结果列表
     */
    @Override
    public List<RetrievalResult> search(String kbId, String queryText, int topK) {
        VectorStore vectorStore = springAiObjectPoolManager.getVectorStoreByKbId(kbId);
        if (vectorStore == null) {
            log.debug("VectorStore not available, returning empty results");
            return Collections.emptyList();
        }
        return doSearch(queryText, topK, vectorStore);
    }



    /**
     * 执行搜索。
     */
    private List<RetrievalResult> doSearch(String queryText, int topK, VectorStore vectorStore) {
        try {
            SearchRequest request = buildSearchRequest(queryText, topK);
            List<Document> results = vectorStore.similaritySearch(request);
            return convertResults(results);
        } catch (Exception e) {
            log.error("Vector search failed", e);
            return List.of();
        }
    }

    /**
     * 构建搜索请求。
     */
    private SearchRequest buildSearchRequest(String queryText, int topK) {
        return SearchRequest.builder()
                .query(queryText)
                .topK(topK)
                .similarityThreshold(0.0)
                .build();
    }

    /**
     * 转换搜索结果。
     */
    private List<RetrievalResult> convertResults(List<Document> results) {
        return results.stream()
                .map(d -> toResult(d))
                .filter(r -> r != null)
                .toList();
    }

    /**
     * 将文档转换为检索结果。
     */
    private RetrievalResult toResult(Document doc) {
        Map<String, Object> meta = doc.getMetadata();
        String docId = meta != null ? String.valueOf(meta.getOrDefault("document_id", "")) : "";
        String content = doc.getText() != null ? doc.getText() : "";
        String id = doc.getId() != null ? doc.getId() : docId;
        return new RetrievalResult(docId, null, id, content, doc.getScore());
    }
}
