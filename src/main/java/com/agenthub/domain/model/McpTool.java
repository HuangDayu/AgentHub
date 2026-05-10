package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * MCP工具领域模型.
 * <p>
 * 表示Model Context Protocol工具配置。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpTool {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String serverUrl;
    private ServerType serverType;
    private String command;
    private List<String> args;
    private Map<String, String> env;
    private boolean async;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public enum ServerType {STDIO, HTTP, SSE}
}
