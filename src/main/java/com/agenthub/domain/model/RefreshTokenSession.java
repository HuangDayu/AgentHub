package com.agenthub.domain.model;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenSession {
    private /** 刷新令牌 */ String token;
    private /** 用户主体（通常是用户ID） */ String subject;
    private /** 过期时间 */ Instant expiresAt;
}
