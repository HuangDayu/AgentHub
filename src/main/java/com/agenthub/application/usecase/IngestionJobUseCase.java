package com.agenthub.application.usecase;

import com.agenthub.application.command.CreateIngestionJobCommand;
import com.agenthub.application.dto.IngestionJobOutput;
import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.repositories.IngestionDocumentRepository;
import com.agenthub.application.port.out.repositories.IngestionJobRepository;
import com.agenthub.domain.exception.JobNotFoundException;
import com.agenthub.domain.model.etl.IngestionDocument;
import com.agenthub.domain.model.etl.IngestionJob;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 入库任务应用服务。
 */
@Component
@RequiredArgsConstructor
public class IngestionJobUseCase {
    private static final Logger log = LoggerFactory.getLogger(IngestionJobUseCase.class);

    private final IngestionJobRepository jobRepository;
    private final IngestionDocumentRepository documentRepository;
    private final IngestionPipelineUseCase pipelineService;
    private final DocumentFileStoragePort documentStorage;

    /**
     * 创建入库任务并触发异步处理流水线。
     *
     * @param command 创建任务命令
     * @return 创建的入库任务
     */
    @Transactional
    public IngestionJob createJob(CreateIngestionJobCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        String jobId = randomId();
        IngestionJob job = IngestionJob.create(jobId, command.getKbId(), 0);

        IngestionJob saved = jobRepository.save(job);
        log.info("Created ingestion job: jobId={}, kbCode={}", saved.getJobId(), saved.getKbId());
        // Trigger pipeline asynchronously
        pipelineService.execute(saved.getJobId());
        return saved;
    }

    /**
     * 上传文档并创建入库任务，触发异步处理流水线。
     */
    @Transactional
    public IngestionJob uploadDocument(
            String kbId, String fileName, String contentType, long size, String storagePath) {
        validateUploadParams(kbId, fileName, storagePath);
        IngestionJob savedJob = createAndSaveJob(kbId);
        IngestionDocument document = createDocument(kbId, savedJob.getJobId(), fileName, contentType, size, storagePath);
        log.info("Document uploaded: jobId={}, documentId={}, storagePath={}",
                savedJob.getJobId(), document.getId(), storagePath);
        pipelineService.execute(savedJob.getJobId());
        return savedJob;
    }

    /**
     * 校验上传参数非空。
     */
    private void validateUploadParams(String kbId, String fileName, String storagePath) {
        Objects.requireNonNull(kbId, "kbCode must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(storagePath, "storagePath must not be null");
    }

    /**
     * 创建并持久化入库任务。
     */
    private IngestionJob createAndSaveJob(String kbId) {
        String jobId = randomId();
        return jobRepository.save(IngestionJob.create(jobId, kbId, 1));
    }

    /**
     * 创建并持久化文档记录。
     */
    private IngestionDocument createDocument(String kbId, String jobId, String fileName,
                                             String contentType, long size, String storagePath) {
        IngestionDocument document = IngestionDocument.createWithStoragePath(
                kbId, jobId, fileName, contentType, size, storagePath);
        return documentRepository.save(document);
    }

    /**
     * 根据任务ID查询入库任务。
     *
     * @param jobId 任务ID
     * @return 入库任务
     */
    public IngestionJob getJobById(String jobId) {
        Objects.requireNonNull(jobId, "jobId must not be null");
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    public IngestionJobOutput uploadDocuments(String kbId, List<MultipartFile> files) {
        IngestionJob job = createJob(kbId, files.size());
        List<IngestionDocument> list = storeFiles(kbId, job.getJobId(), files);
        IngestionJob ingestionJob = job.withDocuments(list);
        pipelineService.execute(ingestionJob);
        log.info("Triggered pipeline for job: jobId={}", job.getJobId());
        return toOutput(job);
    }

    public IngestionJobOutput reVectorStoreDocuments(String kbId, String docId) {
        IngestionDocument document = documentRepository.findByDocId(docId);
        IngestionJob job = createJob(kbId, 1);
        List<IngestionDocument> list = List.of(document);
        IngestionJob ingestionJob = job.withDocuments(list);
        pipelineService.execute(ingestionJob);
        log.info("Triggered pipeline for job: jobId={}", job.getJobId());
        return toOutput(job);
    }

    private IngestionJobOutput toOutput(IngestionJob job) {
        return new IngestionJobOutput(
                job.getJobId(),
                job.getKbId(),
                job.getDocumentCount(),
                job.getStatus().name(),
                job.getCreatedAt()
        );
    }

    private IngestionJob createJob(String kbId, int fileCount) {
        String jobId = generateJobId();
        IngestionJob job = IngestionJob.create(jobId, kbId, fileCount);
        logJobCreation(jobId, kbId, fileCount);
        return jobRepository.save(job);
    }

    private String generateJobId() {
        return java.util.UUID.randomUUID().toString();
    }

    private void logJobCreation(String jobId, String kbId, int fileCount) {
        log.info("Created ingestion job: jobId={}, kbCode={}, fileCount={}", jobId, kbId, fileCount);
    }

    private List<IngestionDocument> storeFiles(String kbId, String jobId, List<MultipartFile> files) {
        List<IngestionDocument> list = new ArrayList<>();
        for (MultipartFile file : files) {
            list.add(storeSingleFile(kbId, jobId, file));
        }
        return list;
    }

    private IngestionDocument storeSingleFile(String kbId, String jobId, MultipartFile file) {
        String documentId = generateDocumentId();
        String objectKey = storeToStorage(kbId, documentId, file);
        return saveDocument(kbId, jobId, file, documentId, objectKey);
    }

    private String generateDocumentId() {
        return java.util.UUID.randomUUID().toString();
    }

    private String storeToStorage(String kbId, String documentId, MultipartFile file) {
        try {
            String objectKey = doStoreToStorage(kbId, documentId, file);
            logStorageSuccess(objectKey, file.getSize());
            return objectKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String doStoreToStorage(String kbId, String documentId, MultipartFile file) throws IOException {
        String storagePath = buildStoragePath(kbId, documentId, file.getOriginalFilename());
        return documentStorage.store(storagePath, file.getInputStream(), file.getSize());
    }

    private String buildStoragePath(String kbId, String documentId, String fileName) {
        return String.format("agenthub/knowledge-bases/%s/documents/%s/%s", kbId, documentId, fileName);
    }

    private void logStorageSuccess(String objectKey, long size) {
        log.info("Stored file to MinIO: objectKey={}, size={}", objectKey, size);
    }

    private IngestionDocument saveDocument(String kbId, String jobId, MultipartFile file, String documentId, String objectKey) {
        IngestionDocument document = createDocument(kbId, jobId, file, objectKey);
        IngestionDocument save = documentRepository.save(document);
        logDocumentSave(save.getId());
        return save;
    }

    private IngestionDocument createDocument(String kbId, String jobId, MultipartFile file, String objectKey) {
        return IngestionDocument.createWithStoragePath(
                kbId, jobId, file.getOriginalFilename(),
                file.getContentType(), file.getSize(), objectKey);
    }

    private void logDocumentSave(String documentId) {
        log.info("Saved document record: documentId={}", documentId);
    }
}
