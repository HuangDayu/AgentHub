package com.agenthub.infrastructure.context.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 租户上下文Web配置器。
 * <p>
 * 注册租户上下文拦截器。
 * </p>
 */
@Configuration
public class TenantContextConfigurer implements WebMvcConfigurer {

    private final String secret;


    public TenantContextConfigurer(@Value("${agenthub.jwt.secret}") String secret) {
        this.secret = secret;
    }

    /**
     * 添加拦截器。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantContextInterceptor(secret)).addPathPatterns("/api/**");
    }
}
