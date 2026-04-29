package com.agenthub.domain.model;

/**
 * 认证令牌对.
 * <p>
 * 包含访问令牌和刷新令牌及其元信息。
 * </p>
 *
 * @param accessToken     访问令牌
 * @param refreshToken    刷新令牌
 * @param tokenType       令牌类型（通常为"Bearer"）
 * @param expiresInSeconds 访问令牌过期时间（秒）
 */
public record AuthTokens(
        /** 访问令牌 */ String accessToken,
        /** 刷新令牌 */ String refreshToken,
        /** 令牌类型（通常为"Bearer"） */ String tokenType,
        /** 访问令牌过期时间（秒） */ long expiresInSeconds
) {
}
