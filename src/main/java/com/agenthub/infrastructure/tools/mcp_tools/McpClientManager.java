package com.agenthub.infrastructure.tools.mcp_tools;

import com.agenthub.domain.model.McpTool;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP客户端管理器，负责创建和管理MCP客户端。
 *
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpClientManager {

    private final Map<String, ToolCallbackProvider> clients = new ConcurrentHashMap<>();
    private final JacksonMcpJsonMapper jacksonMcpJsonMapper = new JacksonMcpJsonMapper(new JsonMapper());

    /**
     * 获取或创建MCP客户端。
     */
    public ToolCallbackProvider getMcpToolCallback(McpTool mcpTool) {
        return clients.computeIfAbsent(mcpTool.id(), id -> createToolCallbackProvider(mcpTool));
    }


    private ToolCallbackProvider createToolCallbackProvider(McpTool mcpTool) {
        McpClientTransport mcpClientTransport = createMcpClientTransport(mcpTool);
        if (mcpTool.async()) {
            McpAsyncClient mcpAsyncClient = McpClient.async(mcpClientTransport).requestTimeout(Duration.ofSeconds(10)).build();
            return AsyncMcpToolCallbackProvider.builder().mcpClients(mcpAsyncClient).build();
        }
        McpSyncClient mcpSyncClient = McpClient.sync(mcpClientTransport).requestTimeout(Duration.ofSeconds(10)).build();
        return SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClient).build();
    }

    private McpClientTransport createMcpClientTransport(McpTool mcpTool) {
        ServerParameters stdioParams = ServerParameters.builder(mcpTool.command())
                .args(mcpTool.args()).env(mcpTool.env()).build();
        return switch (mcpTool.serverType()) {
            case STDIO -> new StdioClientTransport(stdioParams, jacksonMcpJsonMapper);
            case HTTP ->
                    HttpClientStreamableHttpTransport.builder(mcpTool.serverUrl()).jsonMapper(jacksonMcpJsonMapper).build();
            case SSE ->
                    HttpClientSseClientTransport.builder(mcpTool.serverUrl()).jsonMapper(jacksonMcpJsonMapper).build();
        };
    }

    /**
     * 移除客户端。
     */
    public void removeClient(String mcpToolId) {
        clients.remove(mcpToolId);
    }

    /**
     * 清理所有客户端。
     */
    public void clearAll() {
        clients.clear();
    }
}
