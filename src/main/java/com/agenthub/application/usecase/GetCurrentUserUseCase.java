package com.agenthub.application.usecase;

import com.agenthub.application.port.out.JwtTokenProviderPort;
import com.agenthub.domain.model.UserInfo;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 获取当前用户信息用例。
 */
@Component
@RequiredArgsConstructor
public class GetCurrentUserUseCase {
    
    private final JwtTokenProviderPort jwtTokenProvider;

    public UserInfo execute(String token) {
        Claims claims = jwtTokenProvider.validateToken(token);
        return buildUserInfo(claims);
    }

    private UserInfo buildUserInfo(Claims claims) {
        String userId = claims.getSubject();
        String tenantId = claims.get("tenantId", String.class);
        return new UserInfo(userId, userId, tenantId);
    }
}
