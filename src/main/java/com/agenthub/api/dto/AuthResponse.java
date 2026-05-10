package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private /** 访问令牌 */ String accessToken;
    private /** 刷新令牌 */ String refreshToken;
    private /** 令牌类型（通常为"Bearer"） */ String tokenType;
    private /** 访问令牌过期时间（秒） */ long expiresInSeconds;
}
