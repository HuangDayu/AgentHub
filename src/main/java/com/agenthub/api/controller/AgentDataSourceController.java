package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.AgentDataSourceInvokeResponse;
import com.agenthub.api.dto.AgentDataSourceResponse;
import com.agenthub.api.dto.AgentDataSourceTestResponse;
import com.agenthub.api.dto.CreateAgentDataSourceRequest;
import com.agenthub.api.dto.InvokeAgentDataSourceRequest;
import com.agenthub.api.dto.UpdateAgentDataSourceRequest;
import com.agenthub.api.mapper.AgentDataSourceViewMapper;
import com.agenthub.application.command.CreateAgentDataSourceCommand;
import com.agenthub.application.command.InvokeAgentDataSourceCommand;
import com.agenthub.application.command.UpdateAgentDataSourceCommand;
import com.agenthub.application.usecase.AgentDataSourceUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Agent 数据源 Controller - CRUD + 启用/禁用 + 测试/调用
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agent-data-sources")
public class AgentDataSourceController {
    private final AgentDataSourceUseCase useCase;

    public AgentDataSourceController(AgentDataSourceUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<AgentDataSourceResponse> list(@PathVariable String workspaceId) {
        return useCase.list(workspaceId).stream()
            .map(AgentDataSourceViewMapper::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    public AgentDataSourceResponse get(@PathVariable String workspaceId, @PathVariable String id) {
        return AgentDataSourceViewMapper.toResponse(useCase.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentDataSourceResponse create(@PathVariable String workspaceId,
                                          @RequestHeader("X-Tenant-Id") String tenantId,
                                          @RequestBody CreateAgentDataSourceRequest req) {
        CreateAgentDataSourceCommand cmd = BeanUtil.copyProperties(req, CreateAgentDataSourceCommand.class);
        cmd.setTenantId(tenantId);
        cmd.setWorkspaceId(workspaceId);
        return AgentDataSourceViewMapper.toResponse(useCase.create(cmd));
    }

    @PatchMapping("/{id}")
    public AgentDataSourceResponse update(@PathVariable String workspaceId,
                                           @PathVariable String id,
                                           @RequestBody UpdateAgentDataSourceRequest req) {
        UpdateAgentDataSourceCommand cmd = BeanUtil.copyProperties(req, UpdateAgentDataSourceCommand.class);
        return AgentDataSourceViewMapper.toResponse(useCase.update(id, cmd));
    }

    @PostMapping("/{id}/enable")
    public AgentDataSourceResponse enable(@PathVariable String workspaceId, @PathVariable String id) {
        return AgentDataSourceViewMapper.toResponse(useCase.enable(id));
    }

    @PostMapping("/{id}/disable")
    public AgentDataSourceResponse disable(@PathVariable String workspaceId, @PathVariable String id) {
        return AgentDataSourceViewMapper.toResponse(useCase.disable(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId, @PathVariable String id) {
        useCase.delete(id);
    }

    @PostMapping("/{id}/test")
    public AgentDataSourceTestResponse test(@PathVariable String workspaceId, @PathVariable String id) {
        return AgentDataSourceViewMapper.toTestResponse(useCase.test(id));
    }

    @PostMapping("/{id}/invoke")
    public AgentDataSourceInvokeResponse invoke(@PathVariable String workspaceId,
                                                 @PathVariable String id,
                                                 @RequestBody InvokeAgentDataSourceRequest req) {
        InvokeAgentDataSourceCommand cmd = new InvokeAgentDataSourceCommand(
            req.getUserId(), req.getAgentId(), req.getSessionId(), req.getBody(), req.getHeaders()
        );
        return AgentDataSourceViewMapper.toInvokeResponse(useCase.invoke(id, cmd));
    }
}
