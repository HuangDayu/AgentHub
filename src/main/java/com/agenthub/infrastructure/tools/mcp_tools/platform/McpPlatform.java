package com.agenthub.infrastructure.tools.mcp_tools.platform;

/**
 * MCP 平台信息，描述一个 MCP 工具注册平台。
 */
public record McpPlatform(
    String id,
    String name,
    String description,
    String searchApiUrl,
    String detailApiUrl,
    String websiteUrl
) {}
