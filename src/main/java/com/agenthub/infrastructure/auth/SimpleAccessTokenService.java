package com.agenthub.infrastructure.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.domain.model.AccessToken;
import com.agenthub.application.port.out.AccessTokenService;
import com.agenthub.infrastructure.persistence.entity.AppUserEntity;
import com.agenthub.infrastructure.persistence.entity.RoleBindingEntity;
import com.agenthub.infrastructure.persistence.entity.RoleDefEntity;
import com.agenthub.infrastructure.persistence.mapper.AppUserMapper;
import com.agenthub.infrastructure.persistence.mapper.RoleBindingMapper;
import com.agenthub.infrastructure.persistence.mapper.RoleDefMapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于JWT的访问令牌服务实现.
 * <p>
 * 从数据库获取用户租户和角色信息，生成包含这些声明的JWT访问令牌。
 * </p>
 */
public class SimpleAccessTokenService implements AccessTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AppUserMapper appUserMapper;
    private final RoleBindingMapper roleBindingMapper;
    private final RoleDefMapper roleDefMapper;

    /**
     * 构造基于JWT的访问令牌服务。
     *
     * @param jwtTokenProvider JWT令牌提供者
     * @param appUserMapper    用户数据映射器
     * @param roleBindingMapper 角色绑定数据映射器
     * @param roleDefMapper    角色定义数据映射器
     */
    public SimpleAccessTokenService(
            JwtTokenProvider jwtTokenProvider,
            AppUserMapper appUserMapper,
            RoleBindingMapper roleBindingMapper,
            RoleDefMapper roleDefMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.appUserMapper = appUserMapper;
        this.roleBindingMapper = roleBindingMapper;
        this.roleDefMapper = roleDefMapper;
    }

    /**
     * 签发JWT访问令牌，从数据库加载用户租户和角色信息。
     *
     * @param subject 用户标识（通常是用户名）
     * @return 包含JWT令牌和过期时间的访问令牌对象
     */
    @Override
    public AccessToken issueToken(String subject) {
        String tenantId = queryTenantId(subject);
        List<String> roles = queryUserRoles(subject);
        String token = jwtTokenProvider.generateAccessToken(subject, tenantId, roles);
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
     * 查询用户的角色列表，异常时返回空列表。
     */
    private List<String> queryUserRoles(String username) {
        try {
            AppUserEntity user = queryActiveUser(username);
            if (user == null) {
                return Collections.emptyList();
            }
            return fetchRoleCodes(user.getId());
        } catch (Exception ignored) {
            return Collections.emptyList();
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
        return appUserMapper.selectOne(wrapper);
    }

    /**
     * 获取用户的角色编码列表。
     */
    private List<String> fetchRoleCodes(String userId) {
        LambdaQueryWrapper<RoleBindingEntity> bindingWrapper = new LambdaQueryWrapper<>();
        bindingWrapper.eq(RoleBindingEntity::getUserId, userId);
        List<RoleBindingEntity> bindings = roleBindingMapper.selectList(bindingWrapper);
        
        if (bindings.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> roleIds = bindings.stream()
                .map(RoleBindingEntity::getRoleId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<RoleDefEntity> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(RoleDefEntity::getId, roleIds);
        List<RoleDefEntity> roles = roleDefMapper.selectList(roleWrapper);
        
        return roles.stream()
                .map(RoleDefEntity::getRoleCode)
                .collect(Collectors.toList());
    }
}
