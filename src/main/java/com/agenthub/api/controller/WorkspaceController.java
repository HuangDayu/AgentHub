package com.agenthub.api.controller;

import com.agenthub.api.dto.CreateWorkspaceRequest;
import com.agenthub.api.dto.WorkspaceResponse;
import com.agenthub.application.usecase.CreateWorkspaceUseCase;
import com.agenthub.application.usecase.ListWorkspacesUseCase;
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
    private final CreateWorkspaceUseCase createWorkspaceUseCase;
    private final ListWorkspacesUseCase listWorkspacesUseCase;

    /**
     * 构造工作空间控制器。
     *
     * @param createWorkspaceUseCase 创建工作空间用例
     * @param listWorkspacesUseCase  列出工作空间用例
     */
    public WorkspaceController(CreateWorkspaceUseCase createWorkspaceUseCase, ListWorkspacesUseCase listWorkspacesUseCase) {
        this.createWorkspaceUseCase = createWorkspaceUseCase;
        this.listWorkspacesUseCase = listWorkspacesUseCase;
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
        return listWorkspacesUseCase.execute(page, size).stream()
                .map(WorkspaceResponse::from)
                .toList();
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
    public WorkspaceResponse createWorkspaceForTenant(@PathVariable String tenantId, @RequestBody CreateWorkspaceRequest request) {
        Workspace workspace = createWorkspaceUseCase.execute(request.workspaceCode(), request.name(), request.region(), tenantId);
        return WorkspaceResponse.from(workspace);
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
    public WorkspaceResponse createWorkspace(@RequestBody CreateWorkspaceRequest request,
                                             @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID is required via X-Tenant-ID header or path variable");
        }
        Workspace workspace = createWorkspaceUseCase.execute(request.workspaceCode(), request.name(), request.region(), tenantId);
        return WorkspaceResponse.from(workspace);
    }
}
