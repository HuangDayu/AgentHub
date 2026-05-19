package com.agenthub.infrastructure.auth;

import com.agenthub.application.usecase.AuthApplicationUseCase;
import com.agenthub.application.port.out.CredentialVerifierPort;
import com.agenthub.application.port.out.repositories.RefreshTokenRepository;
import com.agenthub.infrastructure.store.db.mapper.AppUserMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;

/**
 * 认证模块基础设施配置.
 * <p>
 * 配置认证相关的Bean，包括JWT令牌提供者、密码编码器和认证应用服务。
 * </p>
 */
@Configuration
public class AuthConfiguration {

    /**
     * 创建JWT令牌提供者Bean。
     *
     * @param secret JWT签名密钥
     * @return JWT令牌提供者实例
     */
    @Bean
    public JwtTokenProvider jwtTokenProvider(@Value("${agenthub.jwt.secret}") String secret) {
        return new JwtTokenProvider(secret);
    }

    /**
     * 创建密码编码器Bean。
     *
     * @return BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建认证应用服务Bean。
     * <p>
     * 优先使用 Spring 容器中已有的 CredentialVerifier Bean（如测试场景下的
     * StaticCredentialVerifier），若不存在则默认使用 DatabaseCredentialVerifier。
     * </p>
     *
     * @param jwtTokenProvider         JWT令牌提供者
     * @param appUserMapper            用户数据映射器
     * @param refreshTokenRepository   刷新令牌仓储
     * @param credentialVerifierProvider 可选的凭据验证器Bean
     * @return 认证应用服务实例
     */
    @Bean
    public AuthApplicationUseCase authApplicationService(
            JwtTokenProvider jwtTokenProvider,
            AppUserMapper appUserMapper,
            RefreshTokenRepository refreshTokenRepository,
            ObjectProvider<CredentialVerifierPort> credentialVerifierProvider) {
        // 优先使用容器中的 CredentialVerifier Bean（测试时注入 StaticCredentialVerifier）
        CredentialVerifierPort credentialVerifierPort = credentialVerifierProvider.getIfAvailable(
                () -> new DatabaseCredentialVerifier(appUserMapper));
        // 创建访问令牌服务
        SimpleAccessTokenAdapter tokenService =
                new SimpleAccessTokenAdapter(jwtTokenProvider, appUserMapper);
        // 组装认证应用服务及其依赖
        return new AuthApplicationUseCase(
                credentialVerifierPort,
                tokenService,
                new UuidRefreshTokenGenerator(),
                refreshTokenRepository,
                Clock.systemUTC(),
                Duration.ofDays(30)
        );
    }
}
