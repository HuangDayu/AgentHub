package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.McpToolCommand;
import com.agenthub.application.dto.McpToolOutput;
import com.agenthub.application.port.out.repositories.McpToolRepository;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class McpToolUseCase {
    private final McpToolRepository repository;

    public McpToolOutput create(McpToolCommand mcpToolCommand) {
        McpTool tool = BeanUtil.copyProperties(mcpToolCommand, McpTool.class);
        return toResult(repository.save(tool));
    }

    public List<McpToolOutput> list(String workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream().map(this::toResult).toList();
    }

    public McpToolOutput get(String id) {
        return toResult(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("MCP Tool not found: " + id)));
    }

    public McpToolOutput update(McpToolCommand mcpToolCommand) {
        McpTool updated = BeanUtil.copyProperties(mcpToolCommand, McpTool.class);
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
        return BeanUtil.copyProperties(tool, McpToolOutput.class);
    }
}
