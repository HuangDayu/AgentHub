package com.agenthub.infrastructure.auth;

import com.agenthub.application.service.AuthApplicationService;
import com.agenthub.application.port.out.CredentialVerifier;
import com.agenthub.application.port.out.repositories.RefreshTokenRepository;
import com.agenthub.infrastructure.persistence.mapper.AppUserMapper;
import com.agenthub.infrastructure.persistence.mapper.RoleBindingMapper;
import com.agenthub.infrastructure.persistence.mapper.RoleDefMapper;
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
    public JwtTokenProvider jwtTokenProvider(@Value("${jwt.secret}") String secret) {
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
     * @param roleBindingMapper        角色绑定数据映射器
     * @param roleDefMapper            角色定义数据映射器
     * @param refreshTokenRepository   刷新令牌仓储
     * @param credentialVerifierProvider 可选的凭据验证器Bean
     * @return 认证应用服务实例
     */
    @Bean
    public AuthApplicationService authApplicationService(
            JwtTokenProvider jwtTokenProvider,
            AppUserMapper appUserMapper,
            RoleBindingMapper roleBindingMapper,
            RoleDefMapper roleDefMapper,
            RefreshTokenRepository refreshTokenRepository,
            ObjectProvider<CredentialVerifier> credentialVerifierProvider) {
        // 优先使用容器中的 CredentialVerifier Bean（测试时注入 StaticCredentialVerifier）
        CredentialVerifier credentialVerifier = credentialVerifierProvider.getIfAvailable(
                () -> new DatabaseCredentialVerifier(appUserMapper));
        // 创建访问令牌服务
        SimpleAccessTokenService tokenService =
                new SimpleAccessTokenService(jwtTokenProvider, appUserMapper, roleBindingMapper, roleDefMapper);
        // 组装认证应用服务及其依赖
        return new AuthApplicationService(
                credentialVerifier,
                tokenService,
                new UuidRefreshTokenGenerator(),
                refreshTokenRepository,
                Clock.systemUTC(),
                Duration.ofDays(30)
        );
    }
}
