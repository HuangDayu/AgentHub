package com.agenthub.api.dto;

import java.util.List;
import java.util.Map;

/**
 * 创建MCP工具请求.
 */
public record CreateMcpToolRequest(
        String name,
        String description,
        String serverUrl,
        String serverType,
        String command,
        List<String> args,
        Map<String, String> env,
        Boolean enabled
) {}
