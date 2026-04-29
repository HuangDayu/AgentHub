package com.agenthub.domain.model;

import java.util.Objects;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 入库文档领域模型，不可变对象。
 * <p>
 * 表示待入库的单个文档，包含文件元数据和处理状态。
 * </p>
 */
public final class IngestionDocument {
    /**
     * 文档唯一标识
     */
    private final String id;
    /**
     * 所属知识库ID
     */
    private final String kbId;
    /**
     * 所属入库任务ID
     */
    private final String jobId;
    /**
     * 文件名
     */
    private final String fileName;
    /**
     * 文件内容类型（MIME）
     */
    private final String contentType;
    /**
     * 文件大小（字节）
     */
    private final long size;
    /**
     * MinIO存储路径
     */
    private final String storagePath;
    /**
     * 文档处理状态
     */
    private final DocumentStatus status;

    private IngestionDocument(
            String id,
            String kbId,
            String jobId,
            String fileName,
            String contentType,
            long size,
            String storagePath,
            DocumentStatus status
    ) {
        this.id = Objects.requireNonNull(id, "documentId must not be null");
        this.kbId = Objects.requireNonNull(kbId, "kbCode must not be null");
        this.jobId = Objects.requireNonNull(jobId, "jobId must not be null");
        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null");
        this.contentType = contentType == null ? "application/octet-stream" : contentType;
        this.size = size;
        this.storagePath = storagePath;
        this.status = status == null ? DocumentStatus.UPLOADED : status;
    }

    /**
     * 创建文档实例，自动生成存储路径。
     *
     * @param kbId        知识库ID
     * @param jobId       任务ID
     * @param fileName    文件名
     * @param contentType 内容类型
     * @param size        文件大小
     * @return 新的IngestionDocument实例
     */
    public static IngestionDocument create(String kbId, String jobId, String fileName, String contentType, long size) {
        String documentId = randomId();
        String storagePath = buildStoragePath(kbId, documentId, fileName);
        return new IngestionDocument(documentId, kbId, jobId, fileName, contentType, size, storagePath, DocumentStatus.UPLOADED);
    }

    /**
     * 使用指定的存储路径创建文档（用于已上传到 MinIO 的文件）。
     */
    public static IngestionDocument createWithStoragePath(
            String kbId, String jobId, String fileName, String contentType, long size, String storagePath) {
        String documentId = randomId();
        return new IngestionDocument(documentId, kbId, jobId, fileName, contentType, size, storagePath, DocumentStatus.UPLOADED);
    }

    /**
     * 从持久化数据重建文档实例。
     *
     * @param documentId  文档ID
     * @param kbId        知识库ID
     * @param jobId       任务ID
     * @param fileName    文件名
     * @param contentType 内容类型
     * @param size        文件大小
     * @param storagePath 存储路径
     * @param status      文档状态
     * @return 重建的IngestionDocument实例
     */
    public static IngestionDocument reconstruct(
            String documentId,
            String kbId,
            String jobId,
            String fileName,
            String contentType,
            long size,
            String storagePath,
            DocumentStatus status
    ) {
        return new IngestionDocument(documentId, kbId, jobId, fileName, contentType, size, storagePath, status);
    }

    private static String buildStoragePath(String kbId, String documentId, String fileName) {
        return String.format("knowledge-bases/%s/documents/%s/%s", kbId, documentId, fileName);
    }

    public IngestionDocument markStatus(DocumentStatus documentStatus) {
        return new IngestionDocument(id, kbId, jobId, fileName, contentType, size, storagePath, documentStatus);
    }

    /**
     * 标记文档状态为已解析。
     */
    public IngestionDocument markParsed() {
        return new IngestionDocument(id, kbId, jobId, fileName, contentType, size, storagePath, DocumentStatus.PARSED);
    }

    /**
     * 标记文档状态为已清洗。
     */
    public IngestionDocument markCleaned() {
        return new IngestionDocument(id, kbId, jobId, fileName, contentType, size, storagePath, DocumentStatus.CLEANED);
    }

    /**
     * 标记文档状态为已分块。
     */
    public IngestionDocument markChunked() {
        return new IngestionDocument(id, kbId, jobId, fileName, contentType, size, storagePath, DocumentStatus.CHUNKED);
    }

    /**
     * 标记文档状态为已向量化。
     */
    public IngestionDocument markVectorized() {
        return new IngestionDocument(id, kbId, jobId, fileName, contentType, size, storagePath, DocumentStatus.VECTORIZED);
    }

    /**
     * 标记文档状态为失败。
     */
    public IngestionDocument markFailed() {
        return new IngestionDocument(id, kbId, jobId, fileName, contentType, size, storagePath, DocumentStatus.FAILED);
    }

    public String getId() {
        return id;
    }

    public String getKbId() {
        return kbId;
    }

    public String getJobId() {
        return jobId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    /**
     * 文档处理状态枚举，标识文档在入库流水线中的当前阶段。
     */
    public enum DocumentStatus {
        UPLOADED,
        PARSED,
        CLEANED,
        CHUNKED,
        VECTORIZED,
        FAILED
    }
}
