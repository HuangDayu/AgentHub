package com.agenthub.common.context.web;

import com.agenthub.common.context.TenantContextHeaders;
import com.agenthub.common.context.TenantContextHolder;
import com.agenthub.common.context.TenantMdcContext;
import com.agenthub.common.context.TenantThreadContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 租户上下文过滤器。
 * <p>
 * 从HTTP请求头中提取租户信息并设置到线程上下文中。
 * </p>
 */
@Deprecated
public class TenantContextFilter extends OncePerRequestFilter {

    /**
     * 执行过滤逻辑。
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        TenantThreadContext context = buildContext(request);
        try (TenantContextHolder.TenantContextScope ignored = TenantContextHolder.open(context)) {
            TenantMdcContext.apply(context);
            filterChain.doFilter(request, response);
        } finally {
            TenantMdcContext.clear();
        }
    }

    /**
     * 从请求构建租户上下文。
     *
     * @param request HTTP请求
     * @return 租户线程上下文
     */
    public static TenantThreadContext buildContext(HttpServletRequest request) {
        return TenantThreadContext.from(
                request.getHeader(TenantContextHeaders.CONTEXT_TENANT_ID),
                request.getHeader(TenantContextHeaders.CONTEXT_WORKSPACE_ID),
                request.getHeader(TenantContextHeaders.CONTEXT_REQUEST_ID)
        );
    }
}
