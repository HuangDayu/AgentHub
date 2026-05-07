package com.agenthub.infrastructure.tools.mcp_tools;

import com.agenthub.application.port.out.repositories.McpToolRepository;
import com.agenthub.domain.model.AgentToolInfo;
import com.agenthub.domain.model.McpTool;
import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agenthub.domain.model.AgentToolType.MCP_TOOL;

/**
 * MCP工具工厂，负责提供MCP工具的ToolCallback。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class McpToolsFactory implements AbstractToolsFactory {

    private final McpToolCallbackProvider mcpToolCallbackProvider;
    private final McpToolRepository mcpToolRepository;

    @Override
    public AgentToolInfo getToolInfo() {
        return new AgentToolInfo(MCP_TOOL);
    }

    @Override
    public Set<ToolCallback> getAllToolCallbacks() {
        return mcpToolCallbackProvider.getToolCallbacks(mcpToolRepository.findList());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(String name) {
        return getAllToolCallbacks().stream()
                .filter(toolCallback -> toolCallback.getToolDefinition().name().equals(name))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(List<AgentToolInfo> toolIds) {
        List<String> list = toolIds.parallelStream().map(AgentToolInfo::getId).toList();
        List<McpTool> mcpTools = mcpToolRepository.findByIds(list);
        return mcpToolCallbackProvider.getToolCallbacks(mcpTools);
    }

}
