package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateSkillFromUrlRequest;
import com.agenthub.api.dto.CreateSkillFromUploadRequest;
import com.agenthub.api.dto.CreateSkillRequest;
import com.agenthub.api.dto.SkillResponse;
import com.agenthub.application.command.CreateSkillCommand;
import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.usecase.SkillUseCase;
import com.agenthub.infrastructure.context.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 技能控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillUseCase useCase;

    /**
     * 创建技能。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@PathVariable String workspaceId,
                                @RequestBody CreateSkillRequest request) {
        CreateSkillCommand createSkillCommand = BeanUtil.copyProperties(request, CreateSkillCommand.class);
        createSkillCommand.setWorkspaceId(workspaceId);
        createSkillCommand.setSkillType("SYNCED");
        createSkillCommand.setSource("LOCAL");
        createSkillCommand.setSourcePath(request.getSkillPath());
        SkillOutput result = useCase.createSynced(createSkillCommand);
        return toResponse(result);
    }

    /**
     * 从 URL 创建技能。
     */
    @PostMapping("/from-url")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse createFromUrl(@PathVariable String workspaceId,
                                       @RequestBody CreateSkillFromUrlRequest request) {
        CreateSkillCommand createSkillCommand = BeanUtil.copyProperties(request, CreateSkillCommand.class);
        createSkillCommand.setWorkspaceId(workspaceId);
        createSkillCommand.setSkillType("UPLOADED");
        createSkillCommand.setSource("URL");
        createSkillCommand.setSourcePath(request.getZipUrl());
        SkillOutput result = useCase.createFromUrl(createSkillCommand);
        return toResponse(result);
    }

    /**
     * 上传 ZIP 创建技能。
     */
    @PostMapping("/from-upload")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse createFromUpload(
            @PathVariable String workspaceId,
            @RequestPart("request") CreateSkillFromUploadRequest request,
            @RequestParam("file") MultipartFile file) throws Exception {
        return toResponse(useCase.createFromUpload(CreateSkillCommand.builder().workspaceId(workspaceId)
                .skillCode(request.getSkillCode()).name(request.getName()).skillType("UPLOADED").source("UPLOAD")
                .sourcePath(file.getOriginalFilename()).description(request.getDescription())
                .zipStream(file.getInputStream()).zipSize(file.getSize()).build()));
    }

    /**
     * 同步所有技能。
     */
    @PostMapping("/sync-all")
    public void syncAll() {
        useCase.syncAll();
    }

    /**
     * 列出技能。
     */
    @GetMapping
    public List<SkillResponse> list() {
        return useCase.list().stream().map(this::toResponse).toList();
    }

    /**
     * 搜索技能。
     */
    @GetMapping("/search")
    public List<SkillResponse> search(@PathVariable String workspaceId,
                                      @RequestParam String keyword) {
        return useCase.search(keyword).stream().map(this::toResponse).toList();
    }

    /**
     * 获取技能。
     */
    @GetMapping("/{skillId}")
    public SkillResponse get(@PathVariable String skillId) {
        return toResponse(useCase.get(skillId));
    }

    /**
     * 更新技能。
     */
    @PutMapping("/{skillId}")
    public SkillResponse update(@PathVariable String skillId,
                                @RequestBody CreateSkillRequest request) {
        SkillOutput result = useCase.update(skillId, request.getName(),
                request.getDescription(), request.getSkillPath());
        return toResponse(result);
    }

    /**
     * 启用技能。
     */
    @PostMapping("/{skillId}/enable")
    public SkillResponse enable(@PathVariable String skillId) {
        return toResponse(useCase.enable(skillId));
    }

    /**
     * 禁用技能。
     */
    @PostMapping("/{skillId}/disable")
    public SkillResponse disable(@PathVariable String skillId) {
        return toResponse(useCase.disable(skillId));
    }

    /**
     * 删除技能。
     */
    @DeleteMapping("/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String skillId) {
        useCase.delete(skillId);
    }

    /**
     * 转换为响应。
     */
    private SkillResponse toResponse(SkillOutput output) {
        SkillResponse response = new SkillResponse();
        BeanUtil.copyProperties(output, response);
        return response;
    }
}
