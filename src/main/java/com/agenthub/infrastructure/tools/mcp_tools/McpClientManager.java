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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
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

    @Getter
    private final Map<String, ToolCallbackProvider> mcpClients = new ConcurrentHashMap<>();
    private final JacksonMcpJsonMapper jacksonMcpJsonMapper = new JacksonMcpJsonMapper(new JsonMapper());

    /**
     * 获取或创建MCP客户端。
     */
    public ToolCallbackProvider getMcpToolCallback(McpTool mcpTool) {
        return mcpClients.computeIfAbsent(mcpTool.id(), id -> createToolCallbackProvider(mcpTool));
    }


    private ToolCallbackProvider createToolCallbackProvider(McpTool mcpTool) {
        McpClientTransport mcpClientTransport = createMcpClientTransport(mcpTool);
        if (mcpTool.async()) {
            return createAsyncMcpToolCallbackProvider(mcpClientTransport, mcpTool);
        }
        Duration timeout = Duration.ofMinutes(5);
        McpSyncClient mcpSyncClient = McpClient.sync(mcpClientTransport)
                .requestTimeout(timeout)
                .initializationTimeout(timeout)
                .build();
        if (!mcpSyncClient.isInitialized()) {
            mcpSyncClient.initialize();
        }
        return SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClient).build();
    }

    private AsyncMcpToolCallbackProvider createAsyncMcpToolCallbackProvider(McpClientTransport mcpClientTransport, McpTool mcpTool) {
        Duration timeout = Duration.ofMinutes(5);
        McpAsyncClient mcpAsyncClient = McpClient.async(mcpClientTransport)
                .requestTimeout(timeout)
                .initializationTimeout(timeout)
                .build();
        if (!mcpAsyncClient.isInitialized()) {
            mcpAsyncClient.initialize();
        }
        return AsyncMcpToolCallbackProvider.builder().mcpClients(mcpAsyncClient).build();

    }

    private McpClientTransport createMcpClientTransport(McpTool mcpTool) {
        ServerParameters stdioParams = createStdioParams(mcpTool);
        return switch (mcpTool.serverType()) {
            case STDIO -> new StdioClientTransport(stdioParams, jacksonMcpJsonMapper);
            case HTTP ->
                    HttpClientStreamableHttpTransport.builder(mcpTool.serverUrl()).jsonMapper(jacksonMcpJsonMapper).build();
            case SSE ->
                    HttpClientSseClientTransport.builder(mcpTool.serverUrl()).jsonMapper(jacksonMcpJsonMapper).build();
        };
    }

    private ServerParameters createStdioParams(McpTool mcpTool) {
        if (isWindows()) {
            List<String> ars = new LinkedList<>();
            ars.add("/c");
            ars.add(mcpTool.command());
            ars.addAll(mcpTool.args());
            return ServerParameters.builder("cmd.exe").args(ars).env(mcpTool.env()).build();
        }
        return ServerParameters.builder(mcpTool.command()).args(mcpTool.args()).env(mcpTool.env()).build();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * 移除客户端。
     */
    public void removeClient(String mcpToolId) {
        mcpClients.remove(mcpToolId);
    }

    /**
     * 清理所有客户端。
     */
    public void clearAll() {
        mcpClients.clear();
    }
}
