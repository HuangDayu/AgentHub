package com.agenthub.infrastructure.tools.mcp_tools.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * MCP 平台注册中心，管理所有 MCP 平台客户端。
 */
@Slf4j
@Component
public class McpPlatformRegistry {

    private final Map<String, McpPlatformClient> clients = new ConcurrentHashMap<>();

    public McpPlatformRegistry(
            @Qualifier("smitheryPlatformClient") McpPlatformClient smithery,
            @Qualifier("glamaPlatformClient") McpPlatformClient glama) {
        register(smithery);
        register(glama);
    }

    public void register(McpPlatformClient client) {
        clients.put(client.getPlatform().id(), client);
        log.info("注册MCP平台: {}", client.getPlatform().name());
    }

    public McpPlatformClient getClient(String platformId) {
        return clients.get(platformId);
    }

    public List<McpPlatform> listPlatforms() {
        return clients.values().stream()
                .map(McpPlatformClient::getPlatform)
                .collect(Collectors.toList());
    }

    public List<McpToolInfo> searchAll(String query, int pageSize) {
        return clients.values().stream()
                .flatMap(client -> client.searchTools(query, pageSize).stream())
                .collect(Collectors.toList());
    }

    public List<McpToolInfo> searchOnPlatform(String platformId, String query, int pageSize) {
        McpPlatformClient client = clients.get(platformId);
        if (client == null) return List.of();
        return client.searchTools(query, pageSize);
    }
}
