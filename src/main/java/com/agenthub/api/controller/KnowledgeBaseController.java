package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.*;
import com.agenthub.api.mapper.IngestionJobResponseMapper;
import com.agenthub.application.command.KnowledgeBaseCommand;
import com.agenthub.application.command.RetrievalCommand;
import com.agenthub.application.dto.IngestionJobOutput;
import com.agenthub.application.dto.RetrievalOutput;
import com.agenthub.application.port.out.rag.RagRetrievalPort;
import com.agenthub.application.usecase.DocumentsUseCase;
import com.agenthub.application.usecase.IngestionJobUseCase;
import com.agenthub.application.usecase.KnowledgeBaseUseCase;
import com.agenthub.domain.model.KnowledgeBase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.agenthub.api.dto.SearchResponse.toSearchResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/knowledge-bases")
public class KnowledgeBaseController {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private final KnowledgeBaseUseCase knowledgeBaseUseCase;
    private final RagRetrievalPort ragRetrievalPort;
    private final DocumentsUseCase documentsUseCase;
    private final IngestionJobUseCase ingestionJobUseCase;
    private final IngestionJobResponseMapper responseMapper;

    @GetMapping
    public KnowledgeBaseListResponse listKnowledgeBases(@PathVariable String workspaceId) {
        List<KnowledgeBase> knowledgeBases = knowledgeBaseUseCase.listByWorkspace(workspaceId);
        return KnowledgeBaseListResponse.from(knowledgeBases);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeBaseResponse createKnowledgeBase(@RequestBody CreateKnowledgeBaseRequest request) {
        KnowledgeBase kb = knowledgeBaseUseCase.create(BeanUtil.copyProperties(request, KnowledgeBaseCommand.class));
        return KnowledgeBaseResponse.from(kb);
    }


    @GetMapping("/{kbId}")
    public KnowledgeBaseResponse getKnowledgeBase(@PathVariable String kbId) {
        return KnowledgeBaseResponse.from(knowledgeBaseUseCase.getById(kbId));
    }

    @PatchMapping("/{kbId}")
    public KnowledgeBaseResponse patchKnowledgeBase(@PathVariable String kbId,
                                                    @RequestBody PatchKnowledgeBaseRequest request) {
        KnowledgeBaseCommand knowledgeBaseCommand = BeanUtil.copyProperties(request, KnowledgeBaseCommand.class);
        knowledgeBaseCommand.setId(kbId);
        KnowledgeBase kb = knowledgeBaseUseCase.update(knowledgeBaseCommand);
        return KnowledgeBaseResponse.from(kb);
    }


    @DeleteMapping("/{kbId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKnowledgeBase(@PathVariable String kbId) {
        knowledgeBaseUseCase.deleteById(kbId);
    }


    /**
     * 执行知识库检索。
     *
     * @param kbId    知识库ID
     * @param request 检索请求参数
     * @return 检索结果
     */
    @PostMapping("/{kbId}/retrieve")
    public SearchResponse retrieve(
            @PathVariable String kbId,
            @RequestBody RetrieveRequest request
    ) {
        RetrievalOutput result = ragRetrievalPort.retrieve(BeanUtil.copyProperties(request, RetrievalCommand.class));
        return toSearchResponse(result);
    }

    /**
     * 流式检索，以SSE方式返回结果。
     *
     * @param kbId    知识库ID
     * @param request 检索请求参数
     * @return 检索结果流
     */
    @GetMapping(value = "/{kbId}/retrieve/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SearchResponse> retrieveStream(
            @PathVariable String kbId,
            @RequestBody RetrieveRequest request
    ) {
        RetrievalOutput result = ragRetrievalPort.retrieve(BeanUtil.copyProperties(request, RetrievalCommand.class));
        return Flux.just(toSearchResponse(result));
    }

    /**
     * 知识库检索端点。
     * <p>
     * 使用检索流水线（查询改写 → 双路检索 → 合并 → 重排 → 过滤 → 引用）执行高级检索。
     * </p>
     *
     * @param kbId    知识库ID
     * @param request 检索请求参数
     * @return 检索结果（含改写后的查询）
     */
    @PostMapping("/{kbId}/search")
    public SearchResponse search(
            @PathVariable String kbId,
            @RequestBody SearchRequest request
    ) {
        RetrievalOutput result = ragRetrievalPort.retrieve(BeanUtil.copyProperties(request, RetrievalCommand.class));
        return toSearchResponse(result);
    }


    @PostMapping(value = "/{kbId}/documents",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreateIngestionJobResponse uploadDocuments(
            @PathVariable String kbId,
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(name = "files", required = false) List<MultipartFile> files) {
        List<MultipartFile> allFiles = mergeFiles(file, files);
        IngestionJobOutput job = ingestionJobUseCase.uploadDocuments(kbId, allFiles);
        return responseMapper.toCreateResponse(job);
    }

    private List<MultipartFile> mergeFiles(MultipartFile file, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) return files;
        if (file != null) return List.of(file);
        return List.of();
    }

    @GetMapping("/{kbId}/documents")
    public List<DocumentResponse> listDocuments(@PathVariable String kbId) {
        var docs = documentsUseCase.listByKbId(kbId);
        return responseMapper.toDocumentResponseList(docs);
    }

    @PutMapping("/{kbId}/documents/{docId}")
    public CreateIngestionJobResponse reVectorStoreDocuments(@PathVariable String kbId, @PathVariable String docId) {
        IngestionJobOutput job = ingestionJobUseCase.reVectorStoreDocuments(kbId, docId);
        return responseMapper.toCreateResponse(job);
    }

    @DeleteMapping("/{kbId}/documents/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable String kbId, @PathVariable String docId) {
        documentsUseCase.deleteById(kbId, docId);
    }

    @GetMapping("/ingestion-jobs/{jobId}")
    public IngestionJobResponse getIngestionJob(@PathVariable String jobId) {
        var job = ingestionJobUseCase.getJobById(jobId);
        return responseMapper.toJobResponse(job);
    }
}
