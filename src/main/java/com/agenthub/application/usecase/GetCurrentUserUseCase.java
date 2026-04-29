package com.agenthub.application.usecase;

import com.agenthub.application.port.out.JwtTokenProviderPort;
import com.agenthub.domain.model.UserInfo;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获取当前用户信息用例。
 */
@Service
public class GetCurrentUserUseCase {
    
    private final JwtTokenProviderPort jwtTokenProvider;

    public GetCurrentUserUseCase(JwtTokenProviderPort jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UserInfo execute(String token) {
        Claims claims = jwtTokenProvider.validateToken(token);
        return buildUserInfo(claims);
    }

    private UserInfo buildUserInfo(Claims claims) {
        String userId = claims.getSubject();
        String tenantId = claims.get("tenantId", String.class);
        List<String> roles = extractRoles(claims);
        return new UserInfo(userId, userId, tenantId, roles);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : List.of();
    }
}
