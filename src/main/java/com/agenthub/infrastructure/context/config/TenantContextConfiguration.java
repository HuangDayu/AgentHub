package com.agenthub.infrastructure.context.config;

import com.agenthub.infrastructure.context.TenantContextGetter;
import com.agenthub.infrastructure.context.TenantContextSupplier;
import com.agenthub.infrastructure.context.TenantThreadContextSupplier;
import com.agenthub.infrastructure.context.handler.TenantContextLineHandler;
import com.agenthub.infrastructure.context.handler.WorkspacesContextLineHandler;
import com.agenthub.infrastructure.context.listener.AgentConfigChangeInterceptor;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 多租户自动配置类。
 * <p>
 * 配置MyBatis-Plus租户拦截器和相关组件。
 * </p>
 */
@Configuration
public class TenantContextConfiguration {

    /**
     * 创建租户上下文获取器。
     */
    @Bean
    public TenantContextGetter tenantContextGetter(List<TenantContextSupplier> tenantContextSupplier) {
        return new TenantContextGetter(tenantContextSupplier);
    }

    /**
     * 创建租户上下文行处理器。
     */
    @Bean
    public TenantContextLineHandler tenantContextLineHandler(TenantContextGetter tenantContextGetter) {
        return new TenantContextLineHandler(tenantContextGetter);
    }

    @Bean
    public WorkspacesContextLineHandler workspacesContextLineHandler(TenantContextGetter tenantContextGetter) {
        return new WorkspacesContextLineHandler(tenantContextGetter);
    }

    /**
     * 创建HTTP上下文租户ID提供者。
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.web.context.request.RequestContextHolder")
    public TenantThreadContextSupplier tenantThreadContextSupplier() {
        return new TenantThreadContextSupplier();
    }

    /**
     * 创建MyBatis-Plus拦截器，包含租户隔离。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            TenantContextLineHandler tenantContextLineHandler,
            WorkspacesContextLineHandler workspacesContextLineHandler,
            AgentConfigChangeInterceptor agentConfigChangeInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantContextLineHandler));
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(workspacesContextLineHandler));
        interceptor.addInnerInterceptor(agentConfigChangeInterceptor);
        return interceptor;
    }


}
