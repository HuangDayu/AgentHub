package com.agenthub.api.controller;

import com.agenthub.api.dto.*;
import com.agenthub.api.dto.*;
import com.agenthub.api.mapper.ToolViewMapper;
import com.agenthub.application.command.CreateToolCommand;
import com.agenthub.application.command.InvokeToolCommand;
import com.agenthub.application.command.UpdateToolCommand;
import com.agenthub.application.usecase.ToolsUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.agenthub.api.mapper.ToolViewMapper.toResponse;

/**
 * 工具 API 控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/tools")
public class ToolController {
    private final ToolsUseCase service;

    public ToolController(ToolsUseCase service) {
        this.service = service;
    }

    @GetMapping
    public List<ToolViewResponse> listTools() {
        return service.listTools().stream()
                .map(ToolViewMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ToolViewResponse createTool(@RequestBody CreateToolRequest request) {
        boolean enabled = resolveEnabled(request);
        return toResponse(service.createTool(buildCreateCommand(request, enabled)));
    }

    private boolean resolveEnabled(CreateToolRequest request) {
        return request.enabled() == null || request.enabled();
    }

    private CreateToolCommand buildCreateCommand(CreateToolRequest request, boolean enabled) {
        return new CreateToolCommand(request.name(), request.description(), enabled);
    }

    @GetMapping("/{toolId}")
    public ToolViewResponse getTool(@PathVariable String toolId) {
        return toResponse(service.getTool(toolId));
    }

    @PatchMapping("/{toolId}")
    public ToolViewResponse updateTool(@PathVariable String toolId, @RequestBody UpdateToolRequest request) {
        return toResponse(service.updateTool(toolId, buildUpdateCommand(request)));
    }

    private UpdateToolCommand buildUpdateCommand(UpdateToolRequest request) {
        return new UpdateToolCommand(request.name(), request.description(), request.enabled());
    }

    @PostMapping("/{toolId}/invoke")
    public ToolInvokeViewResponse invokeTool(
            @PathVariable String toolId,
            @RequestBody(required = false) InvokeToolRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader) {
        String idempotencyKey = resolveIdempotencyKey(request, idempotencyKeyHeader);
        return toResponse(service.invokeTool(toolId, buildInvokeCommand(request, idempotencyKey)));
    }

    private String resolveIdempotencyKey(InvokeToolRequest request, String header) {
        if (request != null && request.idempotencyKey() != null) {
            return request.idempotencyKey();
        }
        return header;
    }

    private InvokeToolCommand buildInvokeCommand(InvokeToolRequest request, String idempotencyKey) {
        return new InvokeToolCommand(idempotencyKey, request == null ? null : request.payload());
    }
}
