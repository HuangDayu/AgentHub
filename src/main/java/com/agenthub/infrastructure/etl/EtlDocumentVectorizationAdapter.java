package com.agenthub.infrastructure.etl;

import com.agenthub.application.port.out.etl.EtlDocumentVectorizationPort;
import com.agenthub.domain.model.DocumentChunk;
import com.agenthub.infrastructure.pool.SpringAiObjectPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Spring AI 向量化实现。
 * <p>
 * 使用分批处理避免超时，每批最多处理 20 个 chunk。
 */
@Component
public class EtlDocumentVectorizationAdapter implements EtlDocumentVectorizationPort {
    private static final Logger log = LoggerFactory.getLogger(EtlDocumentVectorizationAdapter.class);

    /**
     * 每批最大 chunk 数量，避免 llama.cpp 超时
     */
    private static final int BATCH_SIZE = 20;

    /**
     * 文本最大长度，超过则截断
     */
    private static final int MAX_TEXT_LENGTH = 2000;

    /**
     * 匹配无效的 Unicode 代理对
     */
    private static final Pattern INVALID_SURROGATE = Pattern.compile(
            "[\\uD800-\\uDBFF](?![\\uDC00-\\uDFFF])|(?<![\\uD800-\\uDBFF])[\\uDC00-\\uDFFF]|[\uFFFD]"
    );

    private final SpringAiObjectPoolManager springAiObjectPoolManager;

    public EtlDocumentVectorizationAdapter(SpringAiObjectPoolManager springAiObjectPoolManager) {
        this.springAiObjectPoolManager = springAiObjectPoolManager;
    }

    /**
     * 对文档分块进行向量化处理。
     *
     * @param chunks 待向量化的文档分块列表
     * @return 包含嵌入向量的文档分块列表
     */
    @Override
    public List<DocumentChunk> vectorize(String embeddingModelConfigId, List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return chunks;
        List<DocumentChunk> result = new ArrayList<>(chunks.size());
        int totalBatches = calculateTotalBatches(chunks.size());
        EmbeddingModel embeddingModel = springAiObjectPoolManager.getEmbeddingModelByConfigId(embeddingModelConfigId);
        processBatches(embeddingModel, chunks, result, totalBatches);
        logVectorizationResult(result);
        return result;
    }

    /**
     * 计算总批次数。
     */
    private int calculateTotalBatches(int size) {
        return (size + BATCH_SIZE - 1) / BATCH_SIZE;
    }

    /**
     * 分批处理向量化。
     */
    private void processBatches(EmbeddingModel embeddingModel, List<DocumentChunk> chunks, List<DocumentChunk> result, int totalBatches) {
        for (int batch = 0; batch < totalBatches; batch++) {
            int start = batch * BATCH_SIZE;
            int end = Math.min(start + BATCH_SIZE, chunks.size());
            List<DocumentChunk> batchChunks = chunks.subList(start, end);
            log.debug("Processing batch {}/{}: {} chunks", batch + 1, totalBatches, batchChunks.size());
            result.addAll(vectorizeBatch(embeddingModel, batchChunks));
        }
    }

    /**
     * 记录向量化结果日志。
     */
    private void logVectorizationResult(List<DocumentChunk> result) {
        log.info("Vectorized {} chunks → {} dims", result.size(),
                result.isEmpty() ? 0 : result.get(0).getEmbedding().length);
    }

    /**
     * 向量化单个批次的 chunks。
     *
     * @param chunks 当前批次的文档分块
     * @return 包含嵌入向量的文档分块列表
     */
    private List<DocumentChunk> vectorizeBatch(EmbeddingModel embeddingModel, List<DocumentChunk> chunks) {
        List<String> texts = chunks.stream().map(this::prepareText).toList();
        List<float[]> embeddings = embeddingModel.embed(texts);
        return buildResultChunks(chunks, embeddings);
    }

    /**
     * 构建结果分块列表。
     */
    private List<DocumentChunk> buildResultChunks(List<DocumentChunk> chunks, List<float[]> embeddings) {
        List<DocumentChunk> result = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            result.add(chunks.get(i).withEmbedding(embeddings.get(i)));
        }
        return result;
    }

    /**
     * 预处理文本：清理 Unicode 并截断过长内容。
     *
     * @param chunk 文档分块
     * @return 处理后的文本
     */
    private String prepareText(DocumentChunk chunk) {
        String text = cleanUnicode(chunk.getContent());
        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(0, MAX_TEXT_LENGTH);
        }
        return text;
    }

    /**
     * 清理无效的 Unicode 代理对。
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String cleanUnicode(String text) {
        return text == null ? null : INVALID_SURROGATE.matcher(text).replaceAll("");
    }
}
