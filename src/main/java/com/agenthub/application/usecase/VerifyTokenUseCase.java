package com.agenthub.application.usecase;

import com.agenthub.application.port.out.JwtTokenProviderPort;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 令牌验证用例。
 */
@Component
@RequiredArgsConstructor
public class VerifyTokenUseCase {
    
    private final JwtTokenProviderPort jwtTokenProvider;

    public Map<String, Object> execute(String token) {
        Claims claims = jwtTokenProvider.validateToken(token);
        return buildResult(claims);
    }

    private Map<String, Object> buildResult(Claims claims) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid",true);
        result.put("userId", claims.getSubject());
        result.put("tenantId", claims.get("tenantId"));
        result.put("roles", claims.get("roles"));
        return result;
    }
}
