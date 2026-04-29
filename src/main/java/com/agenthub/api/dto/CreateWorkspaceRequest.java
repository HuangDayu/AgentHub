package com.agenthub.api.dto;

/**
 * 创建工作空间请求DTO.
 */
public record CreateWorkspaceRequest(
        /** 工作空间编码 */ String workspaceCode,
        /** 工作空间名称 */ String name,
        /** 区域 */ String region
) {
}

