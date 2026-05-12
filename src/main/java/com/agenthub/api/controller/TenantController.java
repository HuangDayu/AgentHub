package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.*;
import com.agenthub.application.command.PatchTenantCommand;
import com.agenthub.application.command.TenantCommand;
import com.agenthub.application.command.WorkspaceCommand;
import com.agenthub.application.usecase.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户API控制器.
 * <p>
 * 提供租户的增删改查REST API端点，包括创建、查询、列表和更新租户信息。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {
    private final CreateTenantUseCase createTenantUseCase;
    private final ListTenantsUseCase listTenantsUseCase;
    private final GetTenantUseCase getTenantUseCase;
    private final PatchTenantUseCase patchTenantUseCase;
    private final WorkspaceUseCase workspaceUseCase;

    /**
     * 构造租户控制器。
     *
     * @param createTenantUseCase 创建租户用例
     * @param listTenantsUseCase  列出租户用例
     * @param getTenantUseCase    获取租户用例
     * @param patchTenantUseCase  更新租户用例
     * @param workspaceUseCase    工作空间用例
     */
    public TenantController(CreateTenantUseCase createTenantUseCase,
                            ListTenantsUseCase listTenantsUseCase,
                            GetTenantUseCase getTenantUseCase,
                            PatchTenantUseCase patchTenantUseCase,
                            WorkspaceUseCase workspaceUseCase) {
        this.createTenantUseCase = createTenantUseCase;
        this.listTenantsUseCase = listTenantsUseCase;
        this.getTenantUseCase = getTenantUseCase;
        this.patchTenantUseCase = patchTenantUseCase;
        this.workspaceUseCase = workspaceUseCase;
    }

    /**
     * 创建新租户。
     *
     * @param request 创建租户请求
     * @return 创建的租户响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse createTenant(@RequestBody CreateTenantRequest request) {
        var tenant = createTenantUseCase.execute(BeanUtil.copyProperties(request, TenantCommand.class));
        return TenantResponse.from(tenant);
    }

    /**
     * 获取租户列表。
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 租户响应列表
     */
    @GetMapping
    public List<TenantResponse> listTenants(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "20", required = false) int size) {
        return listTenantsUseCase.execute(page, size).stream()
                .map(TenantResponse::from)
                .toList();
    }

    /**
     * 根据ID获取租户详情。
     *
     * @param tenantId 租户ID
     * @return 租户响应
     */
    @GetMapping("/{tenantId}")
    public TenantResponse getTenant(@PathVariable String tenantId) {
        return TenantResponse.from(getTenantUseCase.execute(tenantId));
    }

    /**
     * 部分更新租户信息。
     *
     * @param tenantId 租户ID
     * @param request  更新请求
     * @return 更新后的租户响应
     */
    @PatchMapping("/{tenantId}")
    public TenantResponse patchTenant(@PathVariable String tenantId, @RequestBody PatchTenantRequest request) {
        var tenant = patchTenantUseCase.execute(tenantId, new PatchTenantCommand(request.getName()));
        return TenantResponse.from(tenant);
    }

    /**
     * 在指定租户下创建新工作空间（租户路径参数）。
     *
     * @param tenantId 租户ID（路径参数）
     * @param request  创建工作空间请求
     * @return 创建的工作空间响应
     */
    @PostMapping("/{tenantId}/workspaces")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse createWorkspaceUnderTenant(@PathVariable String tenantId,
                                                        @RequestBody CreateWorkspaceRequest request) {
        var workspace = workspaceUseCase.executeWithTenantValidation(tenantId, BeanUtil.copyProperties(request, WorkspaceCommand.class));
        return WorkspaceResponse.from(workspace);
    }

    /**
     * 获取指定租户下的工作空间列表（租户路径参数）。
     *
     * @param tenantId 租户ID（路径参数）
     * @param page     页码（从0开始）
     * @param size     每页大小
     * @return 工作空间响应列表
     */
    @GetMapping("/{tenantId}/workspaces")
    public List<WorkspaceResponse> listWorkspacesUnderTenant(@PathVariable String tenantId,
                                                             @RequestParam(defaultValue = "0", required = false) int page,
                                                             @RequestParam(defaultValue = "20", required = false) int size) {
        return workspaceUseCase.findWorkspacesByTenantId(tenantId, page, size).stream()
                .map(WorkspaceResponse::from)
                .toList();
    }
}
