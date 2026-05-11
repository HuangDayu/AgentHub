package com.agenthub.infrastructure.context.interceptor;

import com.agenthub.common.annotations.IgnoreTenantContext;
import com.agenthub.infrastructure.context.TenantContextHeaders;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantMdcContext;
import com.agenthub.infrastructure.context.TenantThreadContext;
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

import static com.agenthub.infrastructure.context.TenantContextHeaders.CONTEXT_WORKSPACE_ID;

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
        return new TenantThreadContext(
                getTenantId(request, handler),
                getWorkspaceId(request),
                request.getHeader(TenantContextHeaders.CONTEXT_REQUEST_ID),
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

    public String getWorkspaceId(HttpServletRequest httpServletRequest) {
        String servletPath = httpServletRequest.getServletPath();
        if (servletPath.startsWith("/api/v1/workspaces/")) {
            String substring = servletPath.replaceAll("/api/v1/workspaces/", "");
            if (substring.split("/").length > 1) {
                return substring.substring(0, substring.indexOf("/"));
            }
        }
        return httpServletRequest.getHeader(CONTEXT_WORKSPACE_ID);
    }

    public String getTenantId(HttpServletRequest httpServletRequest, Object handler) {
        if (isIgnoreTenantContext(handler)) {
            return null;
        }
        String token = httpServletRequest.getHeader("Authorization");
        if (token != null) {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token.replaceAll("Bearer ", ""))
                    .getPayload().get("tenantId", String.class);
        }
        throw new JwtException("Unauthorized");
    }
}
