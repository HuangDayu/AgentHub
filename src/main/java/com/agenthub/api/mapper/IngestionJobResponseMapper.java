package com.agenthub.api.mapper;

import com.agenthub.api.dto.CreateIngestionJobResponse;
import com.agenthub.api.dto.DocumentResponse;
import com.agenthub.api.dto.IngestionDocumentResponse;
import com.agenthub.api.dto.IngestionJobResponse;
import com.agenthub.application.dto.IngestionJobOutput;
import com.agenthub.domain.model.IngestionDocument;
import com.agenthub.domain.model.IngestionJob;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 入库任务响应映射器。
 * <p>
 * 负责将领域对象转换为REST API响应DTO。
 * </p>
 */
@Component
public class IngestionJobResponseMapper {

    /**
     * 将入库任务输出DTO转换为创建响应DTO。
     *
     * @param job 入库任务输出DTO
     * @return 创建任务响应DTO
     */
    public CreateIngestionJobResponse toCreateResponse(IngestionJobOutput job) {
        return new CreateIngestionJobResponse(
                job.jobId(),
                job.kbId(),
                job.status(),
                job.fileCount()
        );
    }

    /**
     * 将入库任务转换为创建响应DTO。
     *
     * @param job 入库任务领域对象
     * @return 创建任务响应DTO
     */
    public CreateIngestionJobResponse toCreateResponse(IngestionJob job) {
        return new CreateIngestionJobResponse(
                job.getJobId(),
                job.getKbId(),
                job.getStatus().name(),
                job.getDocumentCount()
        );
    }

    /**
     * 将入库任务转换为查询响应DTO。
     *
     * @param job 入库任务领域对象
     * @return 任务查询响应DTO
     */
    public IngestionJobResponse toJobResponse(IngestionJob job) {
        return new IngestionJobResponse(
                job.getJobId(),
                job.getKbId(),
                job.getStatus().name(),
                job.getDocumentCount(),
                job.getCreatedAt(),
                job.getDocuments().stream().map(this::toIngestionDocumentResponse).toList()
        );
    }

    /**
     * 将入库文档列表转换为文档响应DTO列表。
     *
     * @param documents 入库文档领域对象列表
     * @return 文档响应DTO列表
     */
    public List<DocumentResponse> toDocumentResponseList(List<IngestionDocument> documents) {
        return documents.stream().map(this::toDocumentResponse).toList();
    }

    /**
     * 将入库文档领域对象转换为文档响应DTO。
     *
     * @param document 入库文档领域对象
     * @return 文档响应DTO
     */
    private DocumentResponse toDocumentResponse(IngestionDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getKbId(),
                document.getFileName(),
                document.getContentType(),
                document.getSize(),
                document.getStatus().name(),
                Instant.now()
        );
    }

    /**
     * 将入库文档领域对象转换为简化文档响应DTO。
     *
     * @param document 入库文档领域对象
     * @return 简化文档响应DTO
     */
    public IngestionDocumentResponse toIngestionDocumentResponse(IngestionDocument document) {
        return new IngestionDocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getContentType(),
                document.getSize()
        );
    }
}
