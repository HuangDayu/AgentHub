package com.agenthub.application.usecase;

import com.agenthub.application.dto.McpToolOutput;
import com.agenthub.application.port.out.repositories.McpToolRepository;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class McpToolUseCase {
    private final McpToolRepository repository;

    public McpToolOutput create(String workspaceId, String tenantId, String name, String description,
                                String serverUrl, String serverType, String command,
                                List<String> args, Map<String, String> env, Boolean async, Boolean enabled) {
        McpTool tool = McpTool.create(null, tenantId, workspaceId, name, description,
                serverUrl, parseServerType(serverType), command, args, env, async != null ? async : true,
                enabled != null ? enabled : true);
        return toResult(repository.save(tool));
    }

    public List<McpToolOutput> list(String workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream().map(this::toResult).toList();
    }

    public McpToolOutput get(String id) {
        return toResult(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("MCP Tool not found: " + id)));
    }

    public McpToolOutput update(String id, String name, String description, String serverUrl,
                                String serverType, String command,
                                List<String> args, Map<String, String> env, Boolean async, Boolean enabled) {
        McpTool existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("MCP Tool not found: " + id));
        McpTool updated = existing.patch(name, description, serverUrl,
                parseServerType(serverType), command, args, env, async, enabled);
        return toResult(repository.update(updated));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private McpTool.ServerType parseServerType(String type) {
        if (type == null) return McpTool.ServerType.STDIO;
        return McpTool.ServerType.valueOf(type.toUpperCase());
    }

    private McpToolOutput toResult(McpTool tool) {
        return new McpToolOutput(tool.id(), tool.name(), tool.description(),
                tool.serverUrl(), tool.serverType().name(), tool.command(),
                tool.args(), tool.env(), tool.enabled(),
                tool.createdAt(), tool.updatedAt());
    }
}
