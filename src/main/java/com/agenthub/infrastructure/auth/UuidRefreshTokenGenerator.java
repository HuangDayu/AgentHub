package com.agenthub.infrastructure.auth;

import com.agenthub.application.port.out.RefreshTokenGenerator;

import java.util.UUID;

/**
 * 基于UUID的刷新令牌生成器实现.
 * <p>
 * 使用UUID随机生成刷新令牌。
 * </p>
 */
public class UuidRefreshTokenGenerator implements RefreshTokenGenerator {
    /**
     * 生成刷新令牌。
     *
     * @return UUID格式的刷新令牌字符串
     */
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
