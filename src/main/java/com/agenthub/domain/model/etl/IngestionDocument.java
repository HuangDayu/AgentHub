package com.agenthub.domain.model.etl;

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

    /**
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String kbId;
        private final String jobId;
        private final String fileName;
        private final String contentType;
        private final long size;
        private final String storagePath;

        public CreationSpec(String kbId, String jobId, String fileName,
                               String contentType, long size, String storagePath) {
            this.kbId = kbId;
            this.jobId = jobId;
            this.fileName = fileName;
            this.contentType = contentType;
            this.size = size;
            this.storagePath = storagePath;
        }
    }

    /**
     * 文档字段快照，用于在工厂方法中一次性传入所有字段。
     */
    public static final class Snapshot {
        private final String id;
        private final String kbId;
        private final String jobId;
        private final String fileName;
        private final String contentType;
        private final long size;
        private final String storagePath;
        private final DocumentStatus status;

        public Snapshot(String id, String kbId, String jobId, String fileName,
                        String contentType, long size, String storagePath, DocumentStatus status) {
            this.id = id;
            this.kbId = kbId;
            this.jobId = jobId;
            this.fileName = fileName;
            this.contentType = contentType;
            this.size = size;
            this.storagePath = storagePath;
            this.status = status;
        }
    }

    private IngestionDocument(Snapshot s) {
        this.id = Objects.requireNonNull(s.id, "documentId must not be null");
        this.kbId = Objects.requireNonNull(s.kbId, "kbCode must not be null");
        this.jobId = Objects.requireNonNull(s.jobId, "jobId must not be null");
        this.fileName = Objects.requireNonNull(s.fileName, "fileName must not be null");
        this.contentType = s.contentType == null ? "application/octet-stream" : s.contentType;
        this.size = s.size;
        this.storagePath = s.storagePath;
        this.status = s.status == null ? DocumentStatus.UPLOADED : s.status;
    }

    /**
     * 创建文档实例，自动生成存储路径。
     */
    public static IngestionDocument create(CreationSpec spec) {
        String documentId = randomId();
        String storagePath = buildStoragePath(spec.kbId, documentId, spec.fileName);
        return fromSnapshot(new Snapshot(documentId, spec.kbId, spec.jobId, spec.fileName,
                spec.contentType, spec.size, storagePath, DocumentStatus.UPLOADED));
    }

    /**
     * 使用指定的存储路径创建文档（用于已上传到 MinIO 的文件）。
     */
    public static IngestionDocument createWithStoragePath(CreationSpec spec) {
        String documentId = randomId();
        return fromSnapshot(new Snapshot(documentId, spec.kbId, spec.jobId, spec.fileName,
                spec.contentType, spec.size, spec.storagePath, DocumentStatus.UPLOADED));
    }

    /**
     * 从持久化数据重建文档实例。
     */
    public static IngestionDocument reconstruct(Snapshot s) {
        return fromSnapshot(s);
    }

    private static IngestionDocument fromSnapshot(Snapshot s) {
        return new IngestionDocument(s);
    }

    private static String buildStoragePath(String kbId, String documentId, String fileName) {
        return String.format("knowledge-bases/%s/documents/%s/%s", kbId, documentId, fileName);
    }

    public IngestionDocument markStatus(DocumentStatus documentStatus) {
        return fromSnapshot(new Snapshot(id, kbId, jobId, fileName, contentType, size, storagePath, documentStatus));
    }

    /**
     * 标记文档状态为已解析。
     */
    public IngestionDocument markParsed() {
        return markStatus(DocumentStatus.PARSED);
    }

    /**
     * 标记文档状态为已清洗。
     */
    public IngestionDocument markCleaned() {
        return markStatus(DocumentStatus.CLEANED);
    }

    /**
     * 标记文档状态为已分块。
     */
    public IngestionDocument markChunked() {
        return markStatus(DocumentStatus.CHUNKED);
    }

    /**
     * 标记文档状态为已向量化。
     */
    public IngestionDocument markVectorized() {
        return markStatus(DocumentStatus.VECTORIZED);
    }

    /**
     * 标记文档状态为失败。
     */
    public IngestionDocument markFailed() {
        return markStatus(DocumentStatus.FAILED);
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
