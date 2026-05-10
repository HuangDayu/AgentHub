package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateMemoryRequest;
import com.agenthub.api.dto.MemoryResponse;
import com.agenthub.application.command.MemoryCommand;
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
        MemoryOutput result = useCase.create(BeanUtil.copyProperties(request, MemoryCommand.class));
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
        MemoryCommand command = BeanUtil.copyProperties(request, MemoryCommand.class);
        command.setId(memoryId);
        MemoryOutput result = useCase.update(command);
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
        return BeanUtil.copyProperties(result, MemoryResponse.class);
    }
}
