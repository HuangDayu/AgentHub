package com.agenthub.domain.model;

/**
 * 访问令牌值对象.
 * <p>
 * 封装JWT令牌字符串及其过期时间（秒）。
 * </p>
 *
 * @param tokenValue       JWT令牌字符串
 * @param expiresInSeconds 过期时间（秒）
 */
public record AccessToken(
        /** JWT令牌字符串 */ String tokenValue,
        /** 过期时间（秒） */ long expiresInSeconds
) {
}
