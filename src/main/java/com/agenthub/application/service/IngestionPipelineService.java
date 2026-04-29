package com.agenthub.application.service;

import com.agenthub.common.exception.JobNotFoundException;
import com.agenthub.application.dto.IngestionPipelineError;
import com.agenthub.application.port.out.rag.DocumentChunkerPort;
import com.agenthub.application.port.out.rag.DocumentCleanerPort;
import com.agenthub.application.port.out.rag.DocumentParserPort;
import com.agenthub.application.port.out.rag.DocumentStoragePort;
import com.agenthub.application.port.out.rag.ChunkStorePort;
import com.agenthub.application.port.out.repositories.IngestionDocumentChunkRepository;
import com.agenthub.application.port.out.repositories.IngestionDocumentRepository;
import com.agenthub.application.port.out.repositories.IngestionJobRepository;
import com.agenthub.domain.model.*;
import com.agenthub.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
@Service
public class IngestionPipelineService {
    private static final Logger log = LoggerFactory.getLogger(IngestionPipelineService.class);

    private static final int DEFAULT_CHUNK_SIZE = 512;
    private static final int DEFAULT_OVERLAP_SIZE = 50;

    private final IngestionJobRepository jobRepository;
    private final IngestionDocumentRepository documentRepository;
    private final IngestionDocumentChunkRepository ingestionDocumentChunkRepository;
    private final DocumentStoragePort documentStorage;
    private final DocumentParserPort documentParser;
    private final DocumentCleanerPort contentCleaner;
    private final DocumentChunkerPort documentChunker;
    private final ChunkStorePort chunkStorePort;
    private final ExecutorService ttlExecutorService;

    /**
     * 构造函数，注入流水线所需的各个端口和仓库。
     *
     * @param jobRepository      任务仓库
     * @param documentRepository 文档仓库
     * @param documentStorage    文档存储端口
     * @param documentParser     文档解析端口
     * @param contentCleaner     内容清洗端口
     * @param documentChunker    文档分块端口
     * @param chunkStorePort    分块仓库
     */
    public IngestionPipelineService(
            IngestionJobRepository jobRepository,
            IngestionDocumentRepository documentRepository,
            IngestionDocumentChunkRepository ingestionDocumentChunkRepository,
            DocumentStoragePort documentStorage,
            DocumentParserPort documentParser,
            DocumentCleanerPort contentCleaner,
            DocumentChunkerPort documentChunker,
            ChunkStorePort chunkStorePort,
            @Qualifier("ttlExecutorService") ExecutorService ttlExecutorService
    ) {
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository must not be null");
        this.documentRepository = Objects.requireNonNull(documentRepository, "documentRepository must not be null");
        this.ingestionDocumentChunkRepository = ingestionDocumentChunkRepository;
        this.documentStorage = Objects.requireNonNull(documentStorage, "documentStorage must not be null");
        this.documentParser = Objects.requireNonNull(documentParser, "documentParser must not be null");
        this.contentCleaner = Objects.requireNonNull(contentCleaner, "contentCleaner must not be null");
        this.documentChunker = Objects.requireNonNull(documentChunker, "documentChunker must not be null");
        this.chunkStorePort = Objects.requireNonNull(chunkStorePort, "chunkRepository must not be null");
        this.ttlExecutorService = ttlExecutorService;
    }

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
        List<IngestionDocument> documents = documentRepository.findByJobId(jobId);
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

    /**
     * 完成向量化阶段。
     */
    private IngestionJob completeVectorizing(IngestionJob job, List<DocumentChunk> allChunks) {
        job = transitionTo(job, JobPhase.VECTORIZING);
        chunkStorePort.saveAll(allChunks);
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

    /**
     * 处理单个文档：解析内容、清洗、分块。
     *
     * @param document 入库文档
     * @param job      入库任务
     * @return 该文档的分块列表
     */
    private List<DocumentChunk> processDocument(IngestionDocument document, IngestionJob job) {
        log.info("Job [{}]: processing document [{}]", job.getJobId(), document.getId());

        DocumentContent parsed = parseDocument(document);
        DocumentContent cleaned = contentCleaner.clean(parsed);
        return documentChunker.chunk(
                document.getId(),
                document.getKbId(),
                cleaned.getCleanedContent(),
                DEFAULT_CHUNK_SIZE,
                DEFAULT_OVERLAP_SIZE
        );
    }

    /**
     * 从文档存储中读取并解析文档内容。
     *
     * @param document 入库文档
     * @return 解析后的文档内容
     */
    private DocumentContent parseDocument(IngestionDocument document) {
        try (InputStream content = documentStorage.retrieve(document.getStoragePath())) {
            return documentParser.parse(
                    document.getId(),
                    content,
                    document.getContentType(),
                    document.getFileName()
            );
        } catch (Exception e) {
            throw new IngestionPipelineError(
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
            return jobRepository.save(job.markFailed(ex.getMessage().substring(0, 1000)));
        } catch (Exception persistEx) {
            log.error("Job [{}]: failed to persist failure status", jobId, persistEx);
            throw new IngestionPipelineError("Pipeline failed for job: " + jobId, ex);
        }
    }


}
