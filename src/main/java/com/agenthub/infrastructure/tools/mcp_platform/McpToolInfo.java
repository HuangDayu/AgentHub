package com.agenthub.infrastructure.tools.mcp_platform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MCP 工具搜索结果，从平台 API 返回的工具信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpToolInfo {
    private String qualifiedName;
    private String displayName;
    private String description;
    private String homepage;
    private String installationUrl;
    private String transportType;
    private String packageName;
    private List<String> features;
    private String useCount;
    private String lastUpdated;
    private String platformId;
}
