package com.agenthub.domain.model;

import com.agenthub.domain.enums.JobStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 入库任务领域模型，不可变对象。
 */
public final class IngestionJob {
    private final String jobId;
    private final String kbId;
    private final JobStatus status;
    private final int documentCount;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String errorMessage;
    private final List<IngestionDocument> documents;

    private IngestionJob(
            String jobId, String kbId, JobStatus status, int documentCount,
            Instant createdAt, Instant updatedAt, String errorMessage,
            List<IngestionDocument> documents
    ) {
        this.jobId = Objects.requireNonNull(jobId, "jobId must not be null");
        this.kbId = Objects.requireNonNull(kbId, "kbCode must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.documentCount = documentCount;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = updatedAt;
        this.errorMessage = errorMessage;
        this.documents = Collections.unmodifiableList(new ArrayList<>(documents));
    }

    /**
     * 创建新的入库任务实例，状态为PENDING。
     */
    public static IngestionJob create(String jobId, String kbId, int documentCount) {
        Instant now = Instant.now();
        return new IngestionJob(jobId, kbId, JobStatus.PENDING, documentCount, now, now, null, List.of());
    }

    public IngestionJob withDocuments(List<IngestionDocument> documents) {
        return new IngestionJob(jobId, kbId, status, documentCount, createdAt, updatedAt, errorMessage, documents);
    }

    /**
     * 标记任务状态为解析中。
     */
    public IngestionJob markParsing() {
        List<IngestionDocument> documents = markDocuments(IngestionDocument.DocumentStatus.UPLOADED);
        return new IngestionJob(jobId, kbId, JobStatus.PARSING, documentCount, createdAt, Instant.now(), null, documents);
    }

    /**
     * 标记任务状态为清洗中。
     */
    public IngestionJob markCleaning() {
        List<IngestionDocument> documents = markDocuments(IngestionDocument.DocumentStatus.PARSED);
        return new IngestionJob(jobId, kbId, JobStatus.CLEANING, documentCount, createdAt, Instant.now(), null, documents);
    }

    /**
     * 标记任务状态为分块中。
     */
    public IngestionJob markChunking() {
        List<IngestionDocument> documents = markDocuments(IngestionDocument.DocumentStatus.CLEANED);
        return new IngestionJob(jobId, kbId, JobStatus.CHUNKING, documentCount, createdAt, Instant.now(), null, documents);
    }

    /**
     * 标记任务状态为向量化中。
     */
    public IngestionJob markVectorizing() {
        List<IngestionDocument> documents = markDocuments(IngestionDocument.DocumentStatus.CHUNKED);
        return new IngestionJob(jobId, kbId, JobStatus.VECTORIZING, documentCount, createdAt, Instant.now(), null, documents);
    }

    /**
     * 标记任务状态为已完成。
     */
    public IngestionJob markCompleted() {
        List<IngestionDocument> documents = markDocuments(IngestionDocument.DocumentStatus.VECTORIZED);
        return new IngestionJob(jobId, kbId, JobStatus.COMPLETED, documentCount, createdAt, Instant.now(), null, documents);
    }

    /**
     * 标记任务状态为失败。
     */
    public IngestionJob markFailed(String errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        List<IngestionDocument> documents = markDocuments(IngestionDocument.DocumentStatus.FAILED);
        return new IngestionJob(jobId, kbId, JobStatus.FAILED, documentCount, createdAt, Instant.now(), errorMessage, documents);
    }

    private List<IngestionDocument> markDocuments(IngestionDocument.DocumentStatus documentStatus) {
        List<IngestionDocument> updated = new ArrayList<>();
        for (IngestionDocument document : documents) {
            updated.add(document.markStatus(documentStatus));
        }
        return updated;
    }

    public String getJobId() {
        return jobId;
    }

    public String getKbId() {
        return kbId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<IngestionDocument> getDocuments() {
        return documents;
    }
}
