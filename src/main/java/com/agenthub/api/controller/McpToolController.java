package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateMcpToolRequest;
import com.agenthub.api.dto.McpToolResponse;
import com.agenthub.api.dto.UpdateMcpToolRequest;
import com.agenthub.application.command.McpToolCommand;
import com.agenthub.application.dto.McpToolOutput;
import com.agenthub.application.usecase.McpToolUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/mcp-tools")
public class McpToolController {
    private final McpToolUseCase useCase;

    public McpToolController(McpToolUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public McpToolResponse create(@PathVariable String workspaceId,
                                  @RequestBody CreateMcpToolRequest request) {
        McpToolOutput result = useCase.create(BeanUtil.copyProperties(request, McpToolCommand.class));
        return toResponse(result);
    }

    @GetMapping
    public List<McpToolResponse> list(@PathVariable String workspaceId) {
        return useCase.list(workspaceId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public McpToolResponse get(@PathVariable String workspaceId, @PathVariable String id) {
        return toResponse(useCase.get(id));
    }

    @PutMapping("/{id}")
    public McpToolResponse update(@PathVariable String workspaceId, @PathVariable String id,
                                  @RequestBody UpdateMcpToolRequest request) {
        McpToolCommand mcpToolCommand = BeanUtil.copyProperties(request, McpToolCommand.class);
        mcpToolCommand.setId(id);
        McpToolOutput result = useCase.update(mcpToolCommand);
        return toResponse(result);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId, @PathVariable String id) {
        useCase.delete(id);
    }

    private McpToolResponse toResponse(McpToolOutput result) {
        return BeanUtil.copyProperties(result, McpToolResponse.class);
    }
}
