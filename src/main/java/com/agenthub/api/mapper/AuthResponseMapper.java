package com.agenthub.api.mapper;

import com.agenthub.api.dto.AuthResponse;
import com.agenthub.api.dto.UserInfoResponse;
import com.agenthub.domain.model.AuthTokens;
import com.agenthub.domain.model.UserInfo;
import org.springframework.stereotype.Component;

/**
 * 认证响应映射器。
 * <p>
 * 负责将领域对象转换为REST API响应DTO。
 * </p>
 */
@Component
public class AuthResponseMapper {

    /**
     * 将认证令牌领域对象转换为响应DTO。
     *
     * @param tokens 认证令牌领域对象
     * @return 认证响应DTO
     */
    public AuthResponse toResponse(AuthTokens tokens) {
        return new AuthResponse(
                tokens.accessToken(), tokens.refreshToken(),
                tokens.tokenType(), tokens.expiresInSeconds());
    }

    /**
     * 将用户信息领域对象转换为响应DTO。
     *
     * @param userInfo 用户信息领域对象
     * @return 用户信息响应DTO
     */
    public UserInfoResponse toResponse(UserInfo userInfo) {
        return new UserInfoResponse(
                userInfo.id(), userInfo.username(),
                userInfo.tenantId());
    }
}
