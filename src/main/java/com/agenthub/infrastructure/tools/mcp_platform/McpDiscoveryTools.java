package com.agenthub.infrastructure.tools.mcp_platform;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * MCP 工具发现工具，支持从多个 MCP 平台搜索和发现工具。
 */
@RequiredArgsConstructor
@AgentTools(name = "McpDiscoveryTools", description = "MCP工具发现工具，从Smithery、Glama等平台搜索MCP工具")
public class McpDiscoveryTools {

    private final McpPlatformRegistry platformRegistry;

    @Tool(description = "获取所有支持的MCP工具平台列表")
    public List<McpPlatform> listPlatforms() {
        return platformRegistry.listPlatforms();
    }

    @Tool(description = "在所有MCP平台搜索工具")
    public List<McpToolInfo> searchTools(
            @ToolParam(description = "搜索关键词，如 'database'、'file system'、'github'") String query,
            @ToolParam(description = "返回结果数量，建议10-20") int limit) {
        return platformRegistry.searchAll(query, limit);
    }

    @Tool(description = "在指定平台搜索MCP工具")
    public List<McpToolInfo> searchOnPlatform(
            @ToolParam(description = "平台ID：smithery 或 glama") String platformId,
            @ToolParam(description = "搜索关键词") String query,
            @ToolParam(description = "返回结果数量") int limit) {
        return platformRegistry.searchOnPlatform(platformId, query, limit);
    }

    @Tool(description = "获取MCP工具的详细信息")
    public McpToolInfo getToolDetail(
            @ToolParam(description = "平台ID：smithery 或 glama") String platformId,
            @ToolParam(description = "工具全限定名") String qualifiedName) {
        var client = platformRegistry.getClient(platformId);
        if (client == null) return null;
        return client.getToolDetail(qualifiedName);
    }

    @Tool(description = "获取MCP工具的安装命令")
    public String getInstallationCommand(
            @ToolParam(description = "平台ID：smithery 或 glama") String platformId,
            @ToolParam(description = "工具全限定名") String qualifiedName) {
        var client = platformRegistry.getClient(platformId);
        if (client == null) return "平台不存在: " + platformId;
        return client.getInstallationCommand(qualifiedName);
    }
}
