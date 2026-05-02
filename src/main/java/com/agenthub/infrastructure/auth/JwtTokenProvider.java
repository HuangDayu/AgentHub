package com.agenthub.infrastructure.auth;

import com.agenthub.application.port.out.JwtTokenProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * JWT令牌提供者，基于HMAC-SHA256签名算法。
 */
@Component
public class JwtTokenProvider implements JwtTokenProviderPort {
    private static final long ACCESS_TOKEN_EXPIRY_SECONDS = 7200 * 100;
    private static final long REFRESH_TOKEN_EXPIRY_SECONDS = 604800 * 100;

    private final SecretKey secretKey;

    /**
     * 构造JWT令牌提供者。
     *
     * @param secret HMAC-SHA256签名密钥
     */
    public JwtTokenProvider(@Value("${agenthub.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌，包含用户ID、租户ID和角色信息。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param roles    用户角色列表
     * @return JWT令牌字符串
     */
    @Override
    public String generateAccessToken(String userId, String tenantId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim("tenantId", tenantId)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ACCESS_TOKEN_EXPIRY_SECONDS)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成刷新令牌。
     *
     * @param userId 用户ID
     * @return JWT刷新令牌字符串
     */
    @Override
    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(REFRESH_TOKEN_EXPIRY_SECONDS)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证并解析JWT令牌，返回令牌声明。
     *
     * @param token JWT令牌字符串
     * @return 令牌声明对象
     * @throws JwtException 令牌无效或已过期时抛出
     */
    @Override
    public Claims validateToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取访问令牌的过期时间（秒）。
     *
     * @return 过期秒数
     */
    @Override
    public long getAccessTokenExpirySeconds() {
        return ACCESS_TOKEN_EXPIRY_SECONDS;
    }
}
