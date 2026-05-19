package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.tools.McpTool;

import java.util.List;
import java.util.Optional;

/**
 * MCP工具仓储端口.
 */
public interface McpToolRepository {
    McpTool save(McpTool tool);

    Optional<McpTool> findById(String id);

    List<McpTool> findList();

    List<McpTool> findByWorkspaceId(String workspaceId);

    void deleteById(String id);

    McpTool update(McpTool tool);

    List<McpTool> findByIds(List<String> toolIds);
}
