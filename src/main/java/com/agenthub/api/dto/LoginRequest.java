package com.agenthub.api.dto;

/**
 * 登录请求DTO.
 * <p>
 * 包含用户登录所需的用户名和密码。
 * </p>
 *
 * @param username 用户名
 * @param password 密码
 */
public record LoginRequest(
        /** 用户名 */ String username,
        /** 密码 */ String password
) {
}
