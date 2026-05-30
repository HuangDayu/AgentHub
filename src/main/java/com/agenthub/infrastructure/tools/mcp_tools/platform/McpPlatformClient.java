package com.agenthub.infrastructure.tools.mcp_tools.platform;

import java.util.List;

/**
 * MCP 平台客户端接口，定义从 MCP 工具注册平台搜索和获取工具信息的能力。
 */
public interface McpPlatformClient {

    /**
     * 获取平台信息。
     *
     * @return 平台信息
     */
    McpPlatform getPlatform();

    /**
     * 搜索 MCP 工具。
     *
     * @param query    搜索关键词
     * @param pageSize 每页数量
     * @return 工具列表
     */
    List<McpToolInfo> searchTools(String query, int pageSize);

    /**
     * 获取工具详情。
     *
     * @param qualifiedName 工具全限定名
     * @return 工具信息
     */
    McpToolInfo getToolDetail(String qualifiedName);

    /**
     * 获取工具的安装命令。
     *
     * @param qualifiedName 工具全限定名
     * @return 安装命令或配置
     */
    String getInstallationCommand(String qualifiedName);
}
