package com.agenthub.infrastructure.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.domain.model.auth.AccessToken;
import com.agenthub.application.port.out.AccessTokenPort;
import com.agenthub.infrastructure.store.db.entity.AppUserEntity;
import com.agenthub.infrastructure.store.db.mapper.AppUserMybatisMapper;

/**
 * 基于JWT的访问令牌服务实现.
 * <p>
 * 负责颁发JWT访问令牌，包含用户名、租户ID等信息。
 * </p>
 */
public class SimpleAccessTokenAdapter implements AccessTokenPort {

    private final JwtTokenProvider jwtTokenProvider;
    private final AppUserMybatisMapper appUserMybatisMapper;

    public SimpleAccessTokenAdapter(JwtTokenProvider jwtTokenProvider, AppUserMybatisMapper appUserMybatisMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.appUserMybatisMapper = appUserMybatisMapper;
    }

    /**
     * 颁发访问令牌.
     *
     * @param subject 用户名
     * @return 访问令牌对象
     */
    @Override
    public AccessToken issueToken(String subject) {
        String tenantId = queryTenantId(subject);
        String token = jwtTokenProvider.generateAccessToken(subject, tenantId);
        return new AccessToken(token, jwtTokenProvider.getAccessTokenExpirySeconds());
    }

    /**
     * 查询用户的租户ID，找不到则返回"default"。
     */
    private String queryTenantId(String username) {
        try {
            AppUserEntity user = queryActiveUser(username);
            if (user == null || user.getTenantId() == null) {
                return "default";
            }
            return user.getTenantId();
        } catch (Exception ignored) {
            return "default";
        }
    }

    /**
     * 查询活跃用户。
     */
    private AppUserEntity queryActiveUser(String username) {
        LambdaQueryWrapper<AppUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUserEntity::getUsername, username)
               .eq(AppUserEntity::getStatus, "ACTIVE")
               .last("LIMIT 1");
        return appUserMybatisMapper.selectOne(wrapper);
    }
}
