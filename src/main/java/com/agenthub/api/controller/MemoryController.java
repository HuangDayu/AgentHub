package com.agenthub.api.controller;

import com.agenthub.api.dto.CreateMemoryRequest;
import com.agenthub.api.dto.MemoryResponse;
import com.agenthub.application.dto.MemoryOutput;
import com.agenthub.application.usecase.MemoryUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/memories")
public class MemoryController {
    private final MemoryUseCase useCase;

    public MemoryController(MemoryUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemoryResponse create(@RequestBody CreateMemoryRequest request) {
        MemoryOutput result = useCase.create(request.tenantId(), request.workspaceId(),
                request.agentId(), request.memoryType(), request.content(),
                request.metadata(), request.importance(), request.expiresAt());
        return toResponse(result);
    }

    @GetMapping("/{memoryId}")
    public MemoryResponse get(@PathVariable String memoryId) {
        return toResponse(useCase.get(memoryId));
    }

    @GetMapping("/agents/{agentId}")
    public List<MemoryResponse> listByAgent(@PathVariable String agentId) {
        return useCase.listByAgent(agentId).stream().map(this::toResponse).toList();
    }

    @PutMapping("/{memoryId}")
    public MemoryResponse update(@PathVariable String memoryId,
                                 @RequestBody CreateMemoryRequest request) {
        MemoryOutput result = useCase.update(memoryId, request.content(),
                request.metadata(), request.importance(), request.expiresAt());
        return toResponse(result);
    }

    @DeleteMapping("/{memoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String memoryId) {
        useCase.delete(memoryId);
    }

    @DeleteMapping("/agents/{agentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAgent(@PathVariable String agentId) {
        useCase.deleteByAgent(agentId);
    }

    private MemoryResponse toResponse(MemoryOutput result) {
        return new MemoryResponse(result.id(), result.tenantId(), result.workspaceId(),
                result.agentId(), result.memoryType(), result.content(),
                result.metadata(), result.importance(), result.expiresAt(),
                result.createdAt(), result.updatedAt());
    }
}
