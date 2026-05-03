package com.agenthub.api.controller;

import com.agenthub.api.dto.FunctionToolsResponse;
import com.agenthub.application.dto.FunctionToolOutput;
import com.agenthub.application.usecase.FunctionToolsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/function-tools")
public class FunctionToolsController {

    private final FunctionToolsUseCase useCase;

    public FunctionToolsController(FunctionToolsUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public String sync(@PathVariable String workspaceId) {
        useCase.syncTools();
        return "Tools synced successfully";
    }

    @GetMapping
    public List<FunctionToolsResponse> list(@PathVariable String workspaceId) {
        return useCase.listAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/enabled")
    public List<FunctionToolsResponse> listEnabled(@PathVariable String workspaceId) {
        return useCase.listEnabled().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public FunctionToolsResponse get(@PathVariable String workspaceId, @PathVariable String id) {
        return toResponse(useCase.getById(id));
    }

    @PostMapping("/{id}/enable")
    public FunctionToolsResponse enable(@PathVariable String workspaceId, @PathVariable String id) {
        return toResponse(useCase.enable(id));
    }

    @PostMapping("/{id}/disable")
    public FunctionToolsResponse disable(@PathVariable String workspaceId, @PathVariable String id) {
        return toResponse(useCase.disable(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId, @PathVariable String id) {
        useCase.delete(id);
    }

    private FunctionToolsResponse toResponse(FunctionToolOutput o) {
        return new FunctionToolsResponse(o.id(), o.tenantId(), o.toolClassName(),
            o.toolName(), o.description(), o.category(), o.methodCount(),
            o.enabled(), o.systemTool(), o.createdAt(), o.updatedAt());
    }
}
