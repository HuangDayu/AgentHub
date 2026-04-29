package com.agenthub.api.controller;

import com.agenthub.api.dto.CreatePromptTemplateRequest;
import com.agenthub.api.dto.PromptTemplateResponse;
import com.agenthub.api.dto.UpdatePromptTemplateRequest;
import com.agenthub.api.dto.*;
import com.agenthub.application.dto.PromptTemplateOutput;
import com.agenthub.application.dto.VariableOutput;
import com.agenthub.application.usecase.PromptTemplateUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/prompt-templates")
public class PromptTemplateController {
    private final PromptTemplateUseCase useCase;

    public PromptTemplateController(PromptTemplateUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromptTemplateResponse create(@PathVariable String workspaceId,
                                         @RequestHeader("X-Tenant-Id") String tenantId,
                                         @RequestBody CreatePromptTemplateRequest request) {
        List<VariableOutput> vars = toVariableResults(request.variables());
        PromptTemplateOutput result = useCase.create(workspaceId, tenantId, request.name(), request.description(),
                request.category(), request.content(), vars, request.isActive());
        return toResponse(result);
    }

    @GetMapping
    public List<PromptTemplateResponse> list(@PathVariable String workspaceId,
                                             @RequestParam(required = false) String category) {
        List<PromptTemplateOutput> results = category != null
                ? useCase.listByCategory(workspaceId, category)
                : useCase.list(workspaceId);
        return results.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PromptTemplateResponse get(@PathVariable String workspaceId, @PathVariable String id) {
        return toResponse(useCase.get(id));
    }

    @PutMapping("/{id}")
    public PromptTemplateResponse update(@PathVariable String workspaceId, @PathVariable String id,
                                         @RequestBody UpdatePromptTemplateRequest request) {
        List<VariableOutput> vars = toVariableResultsFromUpdate(request.variables());
        PromptTemplateOutput result = useCase.update(id, request.name(), request.description(),
                request.category(), request.content(), vars, request.isActive());
        return toResponse(result);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId, @PathVariable String id) {
        useCase.delete(id);
    }

    private List<VariableOutput> toVariableResults(List<CreatePromptTemplateRequest.VariableDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(v -> new VariableOutput(v.name(), v.description(), v.defaultValue(), v.required())).toList();
    }

    private List<VariableOutput> toVariableResultsFromUpdate(List<UpdatePromptTemplateRequest.VariableDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(v -> new VariableOutput(v.name(), v.description(), v.defaultValue(), v.required())).toList();
    }

    private PromptTemplateResponse toResponse(PromptTemplateOutput result) {
        List<PromptTemplateResponse.VariableDto> vars = result.variables() != null
                ? result.variables().stream().map(v -> new PromptTemplateResponse.VariableDto(v.name(), v.description(), v.defaultValue(), v.required())).toList()
                : List.of();
        return new PromptTemplateResponse(result.id(), result.name(), result.description(),
                result.category(), result.content(), vars, result.isActive(),
                result.createdAt(), result.updatedAt());
    }
}
