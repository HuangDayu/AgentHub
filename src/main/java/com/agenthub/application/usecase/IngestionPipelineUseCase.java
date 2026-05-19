package com.agenthub.application.usecase;

import com.agenthub.application.command.EtlCommand;
import com.agenthub.domain.exception.IngestionPipelineException;
import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.etl.ExtractTransformLoadPort;
import com.agenthub.application.port.out.repositories.IngestionDocumentChunkRepository;
import com.agenthub.application.port.out.repositories.IngestionDocumentRepository;
import com.agenthub.application.port.out.repositories.IngestionJobRepository;
import com.agenthub.domain.exception.JobNotFoundException;
import com.agenthub.domain.model.etl.DocumentChunk;
import com.agenthub.domain.model.etl.IngestionDocument;
import com.agenthub.domain.model.etl.IngestionJob;
import com.agenthub.domain.enums.JobPhase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 知识入库流水线服务。
 * <p>
 * 状态机: CREATED → PARSING → CLEANING → CHUNKING → VECTORIZING → COMPLETED / FAILED
 * 异步执行，返回{@link CompletableFuture}。
 * </p>
 */
@RequiredArgsConstructor
@Component
public class IngestionPipelineUseCase {
    private static final Logger log = LoggerFactory.getLogger(IngestionPipelineUseCase.class);

    private final IngestionJobRepository jobRepository;
    private final IngestionDocumentRepository documentRepository;
    private final IngestionDocumentChunkRepository ingestionDocumentChunkRepository;
    private final DocumentFileStoragePort documentStorage;
    private final ExtractTransformLoadPort extractTransformLoadPort;
    @Resource(name = "ttlExecutorService")
    private ExecutorService ttlExecutorService;

    public CompletableFuture<IngestionJob> execute(IngestionJob job) {
        Objects.requireNonNull(job, "job must not be null");
        return CompletableFuture.supplyAsync(() -> runPipeline(job), ttlExecutorService)
                .exceptionally(ex -> handlePipelineFailure(job.getJobId(), ex));
    }

    /**
     * 异步启动知识入库流水线。
     *
     * @param jobId 入库任务ID
     * @return 异步执行的CompletableFuture
     */
    public CompletableFuture<IngestionJob> execute(String jobId) {
        Objects.requireNonNull(jobId, "jobId must not be null");
        return CompletableFuture.supplyAsync(() -> runPipeline(jobId), ttlExecutorService)
                .exceptionally(ex -> handlePipelineFailure(jobId, ex));
    }

    private IngestionJob runPipeline(String jobId) {
        IngestionJob job = loadJob(jobId);
        IngestionJob ingestionJob = job.withDocuments(documentRepository.findByJobId(jobId));
        return runPipeline(ingestionJob);
    }

    /**
     * 执行完整的入库流水线。
     *
     * @return 完成后的入库任务
     */
    private IngestionJob runPipeline(IngestionJob job) {
        String jobId = job.getJobId();
        job = transitionTo(job, JobPhase.PARSING);
        List<DocumentChunk> allChunks = processAllDocuments(jobId, job);
        job = completeChunking(job, jobId, allChunks);
        job = completeVectorizing(job, allChunks);
        log.info("Job [{}]: pipeline completed successfully", jobId);
        return job;
    }

    /**
     * 处理所有文档。
     */
    private List<DocumentChunk> processAllDocuments(String jobId, IngestionJob job) {
        List<IngestionDocument> documents = job.getDocuments();
        if (documents == null || documents.isEmpty()) {
            documents = documentRepository.findByJobId(jobId);
        }
        return processDocuments(documents, job);
    }

    /**
     * 完成分块阶段。
     */
    private IngestionJob completeChunking(IngestionJob job, String jobId, List<DocumentChunk> allChunks) {
        job = transitionTo(job, JobPhase.CHUNKING);
        log.info("Job [{}]: chunking completed, total chunks: {}", jobId, allChunks.size());
        return job;
    }

    private IngestionJob completeVectorizing(IngestionJob job, List<DocumentChunk> allChunks) {
        job = transitionTo(job, JobPhase.VECTORIZING);
        ingestionDocumentChunkRepository.saveAll(allChunks);
        return transitionTo(job, JobPhase.COMPLETED);
    }

    /**
     * 批量处理文档列表，对每个文档执行解析、清洗和分块。
     *
     * @param documents 文档列表
     * @param job       入库任务
     * @return 所有文档的分块列表
     */
    private List<DocumentChunk> processDocuments(List<IngestionDocument> documents, IngestionJob job) {
        List<DocumentChunk> allChunks = new ArrayList<>();
        for (IngestionDocument document : documents) {
            List<DocumentChunk> chunks = processDocument(document, job);
            allChunks.addAll(chunks);
        }

        return allChunks;
    }

    private List<DocumentChunk> processDocument(IngestionDocument document, IngestionJob job) {
        try (InputStream content = documentStorage.retrieve(document.getStoragePath())) {
            return extractTransformLoadPort.etl(new EtlCommand(job.getKbId(), document.getId(), content, document.getContentType(), document.getFileName()));
        } catch (Exception e) {
            throw new IngestionPipelineException(
                    "Failed to parse document: " + document.getId(), e);
        }
    }

    /**
     * 根据任务ID加载入库任务，不存在时抛出异常。
     *
     * @param jobId 任务ID
     * @return 入库任务
     */
    private IngestionJob loadJob(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    /**
     * 将任务状态转换到指定阶段并持久化。
     *
     * @param job        当前入库任务
     * @param transition 目标状态转换
     * @return 转换后的入库任务
     */
    private IngestionJob transitionTo(IngestionJob job, JobPhase transition) {
        IngestionJob updated = switch (transition) {
            case PARSING -> job.markParsing();
            case CHUNKING -> job.markChunking();
            case VECTORIZING -> job.markVectorizing();
            case COMPLETED -> job.markCompleted();
        };

        IngestionJob saved = jobRepository.save(updated);
        documentRepository.updateAll(saved.getDocuments());
        log.info("Job [{}]: status transition -> {}", saved.getJobId(), saved.getStatus());
        return saved;
    }

    /**
     * 处理流水线执行失败，将任务标记为失败状态。
     *
     * @param jobId 任务ID
     * @param ex    异常信息
     * @return 标记为失败的入库任务
     */
    private IngestionJob handlePipelineFailure(String jobId, Throwable ex) {
        log.error("Job [{}]: pipeline failed", jobId, ex);
        try {
            IngestionJob job = loadJob(jobId);
            return jobRepository.save(job.markFailed(ex.getMessage().substring(0, Math.min(ex.getMessage().length(), 222))));
        } catch (Exception persistEx) {
            log.error("Job [{}]: failed to persist failure status", jobId, persistEx);
            throw new IngestionPipelineException("Pipeline failed for job: " + jobId, ex);
        }
    }


}
