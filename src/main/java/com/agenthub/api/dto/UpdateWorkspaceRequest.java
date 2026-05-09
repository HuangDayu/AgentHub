package com.agenthub.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 创建工作空间请求DTO.
 */
public record UpdateWorkspaceRequest(
        String id,
        /** 工作空间名称 */ String name,
        /** 区域 */ String region
) {
}

