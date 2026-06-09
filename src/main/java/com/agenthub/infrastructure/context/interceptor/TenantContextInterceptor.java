package com.agenthub.infrastructure.context.interceptor;

import com.agenthub.common.annotations.IgnoreTenantContext;
import com.agenthub.infrastructure.context.TenantContextHeaders;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantMdcContext;
import com.agenthub.infrastructure.context.TenantThreadContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class TenantContextInterceptor implements HandlerInterceptor {

    private final SecretKey secretKey;

    public TenantContextInterceptor(String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!request.getRequestURI().startsWith("/api/")) {
            return true;
        }
        TenantThreadContext context = buildContext(request, handler);
        TenantContextHolder.open(context);
        TenantMdcContext.apply(context);
        return true;
    }

    private TenantThreadContext buildContext(HttpServletRequest request, Object handler) {
        String tenantId = getTenantId(request, handler);
        String userId = getSubject(request);
        return new TenantThreadContext(
                tenantId,
                getPathId(request, "/workspaces/"),
                getPathId(request, "/agents/"),
                getPathId(request, "/sessions/"),
                request.getHeader(TenantContextHeaders.CONTEXT_REQUEST_ID),
                userId,
                isIgnoreTenantContext(handler)
        );
    }

    private boolean isIgnoreTenantContext(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            java.lang.reflect.Method method = handlerMethod.getMethod();
            return method.isAnnotationPresent(IgnoreTenantContext.class);
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, @Nullable Exception ex) throws Exception {
        TenantMdcContext.clear();
        TenantContextHolder.clear();
    }


    public String getPathId(HttpServletRequest httpServletRequest, String path) {
        return parsePathId(requestPath(httpServletRequest), path);
    }

    private String requestPath(HttpServletRequest request) {
        String path = request.getServletPath();
        return path == null || path.isBlank() ? request.getRequestURI() : path;
    }

    private String parsePathId(String requestPath, String path) {
        if (requestPath == null || !requestPath.contains(path)) return null;
        String tail = requestPath.split(path, 2)[1];
        return tail.contains("/") ? tail.split("/", 2)[0] : tail;
    }

    public String getTenantId(HttpServletRequest httpServletRequest, Object handler) {
        if (isIgnoreTenantContext(handler)) {
            return null;
        }
        Claims claims = getClaims(httpServletRequest);
        if (claims != null) {
            return claims.get("tenantId", String.class);
        }
        throw new JwtException("Unauthorized");
    }

    private Claims getClaims(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization");
        if (token != null) {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token.replaceAll("Bearer ", ""))
                    .getPayload();
        }
        return null;
    }

    /**
     * 从 JWT 中提取 subject（用户标识）。
     */
    private String getSubject(HttpServletRequest request) {
        Claims claims = getClaims(request);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }
}
