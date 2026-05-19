package com.agenthub.infrastructure.auth;

import com.agenthub.application.port.out.CredentialVerifierPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

/**
 * 静态凭据验证器.
 * <p>
 * 使用BCrypt加密存储密码，适用于开发和测试环境。
 * 支持从预定义的凭据Map验证用户，或返回固定结果。
 * </p>
 */
@Deprecated
public class StaticCredentialVerifier implements CredentialVerifierPort {
    private final Map<String, String> credentials;
    private final PasswordEncoder passwordEncoder;
    private final boolean fixedResult;

    /**
     * 使用凭据Map构造验证器。
     *
     * @param credentials 用户名到密码哈希的映射
     */
    public StaticCredentialVerifier(Map<String, String> credentials) {
        this.credentials = credentials;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.fixedResult = false;
    }

    /**
     * 使用固定结果构造验证器。
     *
     * @param fixedResult 固定返回的验证结果
     */
    public StaticCredentialVerifier(boolean fixedResult) {
        this.credentials = Map.of();
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.fixedResult = fixedResult;
    }

    /**
     * 验证用户凭据。
     *
     * @param username 用户名
     * @param password 待验证的密码
     * @return 验证通过返回true，否则返回false
     */
    @Override
    public boolean verify(String username, String password) {
        // 如果有预定义凭据，使用BCrypt验证
        if (!credentials.isEmpty()) {
            String storedHash = credentials.get(username);
            return storedHash != null && passwordEncoder.matches(password, storedHash);
        }
        // 否则返回固定结果
        return fixedResult;
    }
}
