package com.agenthub.api.controller;

import com.agenthub.api.dto.PermissionStrategyRequest;
import com.agenthub.api.dto.PermissionStrategyResponse;
import com.agenthub.api.mapper.AgentDataSourceViewMapper;
import com.agenthub.application.command.UpsertPermissionStrategyCommand;
import com.agenthub.application.usecase.PermissionStrategyUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限策略 Controller - 第 5 个策略
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/permission-strategies")
public class PermissionStrategyController {
    private final PermissionStrategyUseCase useCase;

    public PermissionStrategyController(PermissionStrategyUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * 列出工作空间下所有权限策略
     */
    @GetMapping
    public List<PermissionStrategyResponse> list(@PathVariable String workspaceId) {
        return useCase.list(workspaceId).stream()
                .map(AgentDataSourceViewMapper::toResponse)
                .toList();
    }

    /**
     * 获取单个权限策略
     */
    @GetMapping("/{id}")
    public PermissionStrategyResponse get(@PathVariable String workspaceId, @PathVariable String id) {
        return AgentDataSourceViewMapper.toResponse(useCase.get(id));
    }

    /**
     * 创建或更新权限策略
     */
    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionStrategyResponse upsert(@PathVariable String workspaceId,
                                              @RequestHeader("X-Tenant-Id") String tenantId,
                                              @RequestBody PermissionStrategyRequest req) {
        UpsertPermissionStrategyCommand cmd = AgentDataSourceViewMapper.toCommand(req);
        cmd.setTenantId(tenantId);
        cmd.setWorkspaceId(workspaceId);
        return AgentDataSourceViewMapper.toResponse(useCase.upsert(cmd));
    }

    /**
     * 删除权限策略
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId, @PathVariable String id) {
        useCase.delete(id);
    }
}
