package com.agenthub.api.dto;

/**
 * 用户信息响应DTO.
 * <p>
 * 包含当前登录用户的基本信息。
 * </p>
 */
public record UserInfoResponse(
        /** 用户ID */ String id,
        /** 用户名 */ String username,
        /** 租户ID */ String tenantId
) {
}
