package com.agenthub.api.dto;

import java.util.List;

/**
 * 用户信息响应DTO.
 * <p>
 * 包含当前登录用户的基本信息。
 * </p>
 */
public record UserInfoResponse(
        /** 用户ID */ String id,
        /** 用户名 */ String username,
        /** 租户ID */ String tenantId,
        /** 用户角色列表 */ List<String> roles
) {
}
