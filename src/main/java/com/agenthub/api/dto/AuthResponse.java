package com.agenthub.api.dto;

/**
 * 认证响应DTO.
 * <p>
 * 包含用户登录或刷新令牌后返回的认证信息。
 * </p>
 *
 * @param accessToken     访问令牌
 * @param refreshToken    刷新令牌
 * @param tokenType       令牌类型（通常为"Bearer"）
 * @param expiresInSeconds 访问令牌过期时间（秒）
 */
public record AuthResponse(
        /** 访问令牌 */ String accessToken,
        /** 刷新令牌 */ String refreshToken,
        /** 令牌类型（通常为"Bearer"） */ String tokenType,
        /** 访问令牌过期时间（秒） */ long expiresInSeconds
) {
}
