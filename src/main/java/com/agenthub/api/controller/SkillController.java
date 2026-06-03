package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateSkillFromUrlRequest;
import com.agenthub.api.dto.CreateSkillRequest;
import com.agenthub.api.dto.SkillResponse;
import com.agenthub.application.command.CreateSkillCommand;
import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.usecase.SkillUseCase;
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
            @RequestParam(required = false) String skillCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file) throws Exception {
        SkillOutput result = useCase.createFromUpload(CreateSkillCommand.builder()
                .workspaceId(workspaceId)
                .skillCode(skillCode)
                .name(name)
                .skillType("UPLOADED")
                .source("UPLOAD")
                .sourcePath(file.getOriginalFilename())
                .description(description)
                .zipStream(file.getInputStream())
                .zipSize(file.getSize())
                .build());
        return toResponse(result);
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
        response.setId(output.getId());
        response.setTenantId(output.getTenantId());
        response.setWorkspaceId(output.getWorkspaceId());
        response.setSkillCode(output.getSkillCode());
        response.setName(output.getName());
        response.setDescription(output.getDescription());
        response.setSkillType(output.getSkillType());
        response.setSkillPath(output.getSkillPath());
        response.setSkillFilesTree(output.getSkillFilesTree());
        response.setSource(output.getSource());
        response.setSourcePath(output.getSourcePath());
        response.setZipStoragePath(output.getZipStoragePath());
        response.setConfigId(output.getConfigId());
        response.setFileCount(output.getFileCount());
        response.setTotalSize(output.getTotalSize());
        response.setEnabled(output.isEnabled());
        response.setCreatedAt(output.getCreatedAt());
        response.setUpdatedAt(output.getUpdatedAt());
        response.setLastSyncAt(output.getLastSyncAt());
        return response;
    }
}
