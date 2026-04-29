package com.agenthub.api.dto;

/**
 * 令牌验证请求DTO.
 * <p>
 * 包含待验证的访问令牌。
 * </p>
 *
 * @param accessToken 访问令牌
 */
public record VerifyRequest(
        /** 待验证的访问令牌 */ String accessToken
) {
}
