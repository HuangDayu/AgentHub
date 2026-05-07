package com.agenthub.api.controller;

import com.agenthub.api.dto.SystemToolsResponse;
import com.agenthub.application.dto.SystemToolOutput;
import com.agenthub.application.usecase.SystemToolsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/system-tools")
public class SystemToolsController {

    private final SystemToolsUseCase useCase;

    public SystemToolsController(SystemToolsUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public String sync(@PathVariable String workspaceId) {
        useCase.syncTools();
        return "Tools synced successfully";
    }

    @GetMapping
    public List<SystemToolsResponse> list(@PathVariable String workspaceId) {
        return useCase.listAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/enabled")
    public List<SystemToolsResponse> listEnabled(@PathVariable String workspaceId) {
        return useCase.listEnabled().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public SystemToolsResponse get(@PathVariable String workspaceId, @PathVariable String id) {
        return toResponse(useCase.getById(id));
    }

    @PostMapping("/{id}/enable")
    public SystemToolsResponse enable(@PathVariable String workspaceId, @PathVariable String id) {
        return toResponse(useCase.enable(id));
    }

    @PostMapping("/{id}/disable")
    public SystemToolsResponse disable(@PathVariable String workspaceId, @PathVariable String id) {
        return toResponse(useCase.disable(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId, @PathVariable String id) {
        useCase.delete(id);
    }

    private SystemToolsResponse toResponse(SystemToolOutput o) {
        return new SystemToolsResponse(o.id(), o.tenantId(), o.toolClassName(),
            o.toolName(), o.description(), o.category(), o.methodCount(),
            o.enabled(), o.systemTool(), o.createdAt(), o.updatedAt());
    }
}
