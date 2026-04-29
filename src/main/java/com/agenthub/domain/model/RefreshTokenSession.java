package com.agenthub.domain.model;

import java.time.Instant;

/**
 * 刷新令牌会话.
 * <p>
 * 记录刷新令牌与用户主体的关联及其过期时间。
 * </p>
 *
 * @param token     刷新令牌
 * @param subject   用户主体（通常是用户ID）
 * @param expiresAt 过期时间
 */
public record RefreshTokenSession(
        /** 刷新令牌 */ String token,
        /** 用户主体（通常是用户ID） */ String subject,
        /** 过期时间 */ Instant expiresAt
) {
}
