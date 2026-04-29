package com.agenthub.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * 文档分块领域模型，不可变对象。
 * <p>
 * 表示文档经过分块处理后的内容片段，包含文本内容和向量嵌入。
 * </p>
 */
public final class DocumentChunk {
    /** 分块唯一标识 */
    private final String chunkId;
    /** 所属文档ID */
    private final String documentId;
    /** 所属知识库ID */
    private final String kbId;
    /** 分块在文档中的索引位置 */
    private final int chunkIndex;
    /** 分块文本内容 */
    private final String content;
    /** 分块的token数量 */
    private final int tokenCount;
    /** 分块的向量嵌入 */
    private final float[] embedding;

    private DocumentChunk(
            String chunkId,
            String documentId,
            String kbId,
            int chunkIndex,
            String content,
            int tokenCount,
            float[] embedding
    ) {
        this.chunkId = Objects.requireNonNull(chunkId, "chunkId must not be null");
        this.documentId = Objects.requireNonNull(documentId, "documentId must not be null");
        this.kbId = Objects.requireNonNull(kbId, "kbCode must not be null");
        this.chunkIndex = chunkIndex;
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.tokenCount = tokenCount;
        this.embedding = embedding;
    }

    /**
     * 创建新的文档分块实例。
     *
     * @param documentId 文档ID
     * @param kbId       知识库ID
     * @param chunkIndex 分块索引
     * @param content    分块内容
     * @return 新的DocumentChunk实例
     */
    public static DocumentChunk create(String documentId, String kbId, int chunkIndex, String content) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(kbId, "kbCode must not be null");
        Objects.requireNonNull(content, "content must not be null");
        return new DocumentChunk(
                UUID.randomUUID().toString(),
                documentId,
                kbId,
                chunkIndex,
                content,
                estimateTokenCount(content),
                null
        );
    }

    /**
     * 从持久化数据重建分块实例。
     *
     * @param chunkId    分块ID
     * @param documentId 文档ID
     * @param kbId       知识库ID
     * @param chunkIndex 分块索引
     * @param content    分块内容
     * @param tokenCount token数量
     * @param embedding  向量嵌入
     * @return 重建的DocumentChunk实例
     */
    public static DocumentChunk reconstruct(
            String chunkId,
            String documentId,
            String kbId,
            int chunkIndex,
            String content,
            int tokenCount,
            float[] embedding
    ) {
        return new DocumentChunk(chunkId, documentId, kbId, chunkIndex, content, tokenCount, embedding);
    }

    private static int estimateTokenCount(String content) {
        return content.split("\\s+").length;
    }

    /**
     * 返回带有向量嵌入的新实例。
     *
     * @param embedding 向量嵌入数组
     * @return 包含嵌入的新DocumentChunk实例
     */
    public DocumentChunk withEmbedding(float[] embedding) {
        Objects.requireNonNull(embedding, "embedding must not be null");
        return new DocumentChunk(chunkId, documentId, kbId, chunkIndex, content, tokenCount, embedding);
    }

    public String getChunkId() {
        return chunkId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getKbId() {
        return kbId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public float[] getEmbedding() {
        return embedding;
    }
}
