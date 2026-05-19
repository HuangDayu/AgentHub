package com.agenthub.application.usecase;

import com.agenthub.domain.exception.InvalidCredentialsException;
import com.agenthub.domain.exception.InvalidRefreshTokenException;
import com.agenthub.domain.model.AccessToken;
import com.agenthub.domain.model.AuthTokens;
import com.agenthub.domain.model.RefreshTokenSession;
import com.agenthub.application.port.out.AccessTokenPort;
import com.agenthub.application.port.out.CredentialVerifierPort;
import com.agenthub.application.port.out.RefreshTokenGenerator;
import com.agenthub.application.port.out.repositories.RefreshTokenRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * 认证应用服务.
 * <p>
 * 负责用户登录、刷新令牌、登出等认证业务流程的编排。
 * 协调凭据验证、令牌生成和令牌存储等基础设施组件。
 * </p>
 */
public class AuthApplicationUseCase {
    private final CredentialVerifierPort credentialVerifierPort;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;
    private final Duration refreshTokenTtl;

    /**
     * 构造认证应用服务。
     *
     * @param credentialVerifierPort     凭据验证器
     * @param accessTokenPort     访问令牌服务
     * @param refreshTokenGenerator  刷新令牌生成器
     * @param refreshTokenRepository 刷新令牌仓储
     * @param clock                  时钟
     * @param refreshTokenTtl        刷新令牌有效期
     */
    public AuthApplicationUseCase(
            CredentialVerifierPort credentialVerifierPort,
            AccessTokenPort accessTokenPort,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenRepository refreshTokenRepository,
            Clock clock,
            Duration refreshTokenTtl
    ) {
        this.credentialVerifierPort = credentialVerifierPort;
        this.accessTokenPort = accessTokenPort;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    /**
     * 用户登录，验证凭据后签发令牌对。
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证令牌（访问令牌和刷新令牌）
     * @throws InvalidCredentialsException 凭据无效时抛出
     */
    public AuthTokens login(String username, String password) {
        if (!credentialVerifierPort.verify(username, password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return issueTokens(username);
    }

    /**
     * 使用刷新令牌获取新的令牌对，旧令牌随即失效。
     *
     * @param refreshToken 刷新令牌
     * @return 新的认证令牌
     * @throws InvalidRefreshTokenException 刷新令牌无效或已过期时抛出
     */
    public AuthTokens refresh(String refreshToken) {
        RefreshTokenSession existingSession = refreshTokenRepository.findByToken(refreshToken)
                .filter(session -> !session.getExpiresAt().isBefore(Instant.now(clock)))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        refreshTokenRepository.deleteByToken(refreshToken);
        return issueTokens(existingSession.getSubject());
    }

    /**
     * 用户登出，删除指定的刷新令牌使其失效。
     *
     * @param refreshToken 需要失效的刷新令牌
     * @throws InvalidRefreshTokenException 刷新令牌无效时抛出
     */
    public void logout(String refreshToken) {
        if (refreshTokenRepository.findByToken(refreshToken).isEmpty()) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    /**
     * 签发新的令牌对（访问令牌和刷新令牌）。
     * <p>
     * 生成访问令牌和刷新令牌，将刷新令牌保存到仓储中。
     * </p>
     *
     * @param subject 令牌主题（通常是用户ID）
     * @return 包含访问令牌和刷新令牌的认证令牌对象
     */
    private AuthTokens issueTokens(String subject) {
        // 生成访问令牌
        AccessToken accessToken = accessTokenPort.issueToken(subject);
        // 生成刷新令牌
        String refreshToken = refreshTokenGenerator.generate();
        // 计算刷新令牌过期时间
        Instant expiresAt = Instant.now(clock).plus(refreshTokenTtl);
        // 保存刷新令牌会话
        refreshTokenRepository.save(new RefreshTokenSession(refreshToken, subject, expiresAt));
        // 返回完整的认证令牌响应
        return new AuthTokens(accessToken.getTokenValue(), refreshToken, "Bearer", accessToken.getExpiresInSeconds());
    }
}
