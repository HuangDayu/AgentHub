package com.agenthub.api.dto;

/**
 * 刷新令牌请求DTO.
 * <p>
 * 包含刷新令牌，用于获取新的访问令牌。
 * </p>
 *
 * @param refreshToken 刷新令牌
 */
public record RefreshRequest(
        /** 刷新令牌 */ String refreshToken
) {
}
