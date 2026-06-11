package com.agenthub.infrastructure.auth;

import com.agenthub.infrastructure.store.db.mapper.AppUserMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.CredentialVerifierPort;
import com.agenthub.infrastructure.store.db.entity.AppUserEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 基于数据库的凭据验证器.
 * <p>
 * 从 app_user 表查询 password_hash，使用 BCrypt 验证密码。
 * 仅验证状态为 ACTIVE 的用户账户。
 * </p>
 */
public class DatabaseCredentialVerifier implements CredentialVerifierPort {
    private final AppUserMybatisMapper appUserMybatisMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造数据库凭据验证器。
     *
     * @param appUserMybatisMapper MyBatis-Plus用户数据映射器
     */
    public DatabaseCredentialVerifier(AppUserMybatisMapper appUserMybatisMapper) {
        this.appUserMybatisMapper = appUserMybatisMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 验证用户凭据是否有效。
     *
     * @param username 用户名
     * @param password 待验证的明文密码
     * @return 如果凭据有效返回true，否则返回false
     */
    @Override
    public boolean verify(String username, String password) {
        try {
            return verifyCredentials(username, password);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifyCredentials(String username, String password) {
        AppUserEntity user = queryActiveUser(username);
        return user != null && user.getPasswordHash() != null
                && passwordEncoder.matches(password, user.getPasswordHash());
    }

    /**
     * 查询活跃用户。
     */
    private AppUserEntity queryActiveUser(String username) {
        LambdaQueryWrapper<AppUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUserEntity::getUsername, username);
        wrapper.eq(AppUserEntity::getStatus, "ACTIVE");
        return appUserMybatisMapper.selectOne(wrapper);
    }
}
