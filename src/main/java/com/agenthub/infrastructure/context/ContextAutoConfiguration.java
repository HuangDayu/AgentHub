package com.agenthub.infrastructure.context;

import com.agenthub.application.port.out.IdGenerator;
import com.agenthub.application.port.out.TimeProvider;
import com.agenthub.common.utils.RandomUtils;
import com.agenthub.common.utils.TtlUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.agenthub.infrastructure.context.handler.TenantContextLineHandler;
import com.agenthub.infrastructure.context.handler.WorkspacesContextLineHandler;
import com.agenthub.infrastructure.context.listener.AgentConfigChangeInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 多租户自动配置类。
 * <p>
 * 配置MyBatis-Plus租户拦截器和相关组件。
 * </p>
 */
@Configuration
public class ContextAutoConfiguration {

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

    @Bean("ttlExecutorService")
    public ExecutorService ttlExecutorService(){
        return TtlUtils.getTtlExecutorService();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator() {
        return RandomUtils::randomId;
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeProvider timeProvider() {
        return Instant::now;
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
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
