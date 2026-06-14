package com.agenthub.infrastructure.tools.mcp_tools;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.agenthub.domain.model.tools.McpTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MCP工具回调提供者，将McpTool配置转换为ToolCallback。
 *
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolCallbackProvider {


    private final McpClientManager mcpClientManager;

    /**
     * 根据workspaceId获取所有MCP工具的ToolCallback。
     */
    public Set<ToolCallback> getToolCallbacks(List<McpTool> mcpTools) {
        return convertToToolCallbacks(mcpTools);
    }

    /**
     * 将McpTool列表转换为ToolCallback集合。
     */
    private Set<ToolCallback> convertToToolCallbacks(List<McpTool> mcpTools) {
        Set<String> names = new ConcurrentHashSet<>();
        return mcpTools.parallelStream()
                .filter(v -> {
                    if (v.isEnabled() && !names.contains(v.getName())) {
                        names.add(v.getName());
                        return true;
                    }
                    return false;
                })
                .map(v -> createToolCallback(v).getToolCallbacks())
                .flatMap(v -> Set.of(v).stream())
                .collect(Collectors.toSet());
    }

    /**
     * 判断MCP工具是否启用。
     */
    private boolean isEnabled(McpTool mcpTool) {
        return mcpTool.isEnabled();
    }


    /**
     * 创建同步MCP工具回调。
     */
    private ToolCallbackProvider createToolCallback(McpTool mcpTool) {
        return mcpClientManager.getMcpToolCallback(mcpTool);
    }
}
