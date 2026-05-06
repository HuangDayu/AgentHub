package com.agenthub.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * MCP工具领域模型.
 * <p>
 * 表示Model Context Protocol工具配置。
 * </p>
 */
public record McpTool(
        String id,
        String tenantId,
        String workspaceId,
        String name,
        String description,
        String serverUrl,
        ServerType serverType,
        String command,
        List<String> args,
        Map<String, String> env,
        boolean async,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public enum ServerType {STDIO, HTTP, SSE}

    public static McpTool create(String id, String tenantId, String workspaceId,
                                 String name, String description, String serverUrl,
                                 ServerType serverType, String command,
                                 List<String> args, Map<String, String> env, boolean async, boolean enabled) {
        Instant now = Instant.now();
        return new McpTool(id, tenantId, workspaceId, name.trim(), description,
                serverUrl, serverType, command, args, env, async, enabled, now, now);
    }

    public McpTool patch(String name, String description, String serverUrl,
                         ServerType serverType, String command,
                         List<String> args, Map<String, String> env, Boolean async, Boolean enabled) {
        return new McpTool(this.id, this.tenantId, this.workspaceId,
                resolveName(name), resolveDescription(description),
                resolveServerUrl(serverUrl), resolveServerType(serverType),
                resolveCommand(command), resolveArgs(args), resolveEnv(env), resolveEnabled(async),
                resolveEnabled(enabled), this.createdAt, Instant.now());
    }

    private String resolveName(String name) {
        return name == null ? this.name : name.trim();
    }

    private String resolveDescription(String desc) {
        return desc == null ? this.description : desc;
    }

    private String resolveServerUrl(String url) {
        return url == null ? this.serverUrl : url;
    }

    private ServerType resolveServerType(ServerType type) {
        return type == null ? this.serverType : type;
    }

    private String resolveCommand(String cmd) {
        return cmd == null ? this.command : cmd;
    }

    private List<String> resolveArgs(List<String> args) {
        return args == null ? this.args : args;
    }

    private Map<String, String> resolveEnv(Map<String, String> env) {
        return env == null ? this.env : env;
    }

    private boolean resolveEnabled(Boolean enabled) {
        return enabled == null ? this.enabled : enabled;
    }
}
