package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateWorkspaceRequest;
import com.agenthub.api.dto.UpdateWorkspaceRequest;
import com.agenthub.api.dto.WorkspaceResponse;
import com.agenthub.application.command.WorkspaceCommand;
import com.agenthub.application.usecase.WorkspaceUseCase;
import com.agenthub.domain.model.Workspace;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作空间API控制器.
 * <p>
 * 提供租户下工作空间的增删改查REST API端点。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {
    private final WorkspaceUseCase workspaceUseCase;

    /**
     * 构造工作空间控制器。
     *
     * @param workspaceUseCase 创建工作空间用例
     * @param workspaceUseCase 列出工作空间用例
     */
    public WorkspaceController(WorkspaceUseCase workspaceUseCase) {
        this.workspaceUseCase = workspaceUseCase;
    }

    /**
     * 获取工作空间列表，支持分页。
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 工作空间响应列表
     */
    @GetMapping
    public List<WorkspaceResponse> listWorkspaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return workspaceUseCase.execute(page, size).stream()
                .map(WorkspaceResponse::from)
                .toList();
    }

    /**
     * 创建新工作空间（全局，不带租户路径参数 - 已弃用，需要提供tenantId请求头）。
     * 为了兼容保留，但需要请求头X-Tenant-ID。
     *
     * @param request 创建工作空间请求
     * @return 创建的工作空间响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse createWorkspace(@RequestBody CreateWorkspaceRequest request) {
        Workspace workspace = workspaceUseCase.execute(BeanUtil.copyProperties(request, WorkspaceCommand.class));
        return WorkspaceResponse.from(workspace);
    }

    /**
     * 在指定租户下创建新工作空间（租户路径参数）。
     *
     * @param tenantId 租户ID（路径参数）
     * @param request  创建工作空间请求
     * @return 创建的工作空间响应
     */
    @PostMapping("/tenants/{tenantId}/workspaces")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse createWorkspaceForTenant(@PathVariable String tenantId,
                                                       @RequestBody CreateWorkspaceRequest request) {
        var workspace = workspaceUseCase.executeWithTenantValidation(tenantId, BeanUtil.copyProperties(request, WorkspaceCommand.class));
        return WorkspaceResponse.from(workspace);
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable String id, @RequestBody UpdateWorkspaceRequest updateWorkspaceRequest) {
        workspaceUseCase.update(id, BeanUtil.copyProperties(updateWorkspaceRequest, WorkspaceCommand.class));
    }
}
