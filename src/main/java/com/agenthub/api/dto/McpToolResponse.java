package com.agenthub.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * MCP工具响应.
 */
public record McpToolResponse(
        String id,
        String name,
        String description,
        String serverUrl,
        String serverType,
        String command,
        List<String> args,
        Map<String, String> env,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
