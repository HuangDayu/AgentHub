package com.agenthub.infrastructure.context;


import com.agenthub.common.annotations.IgnoreTenantContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 租户上下文AOP切面。
 * <p>
 * 拦截带有 {@link IgnoreTenantContext} 注解的定时任务方法，
 * 在执行前自动设置租户上下文（忽略租户隔离）。
 * </p>
 *
 * @author huangdayu
 */
@Slf4j
@Aspect
@Component
public class TenantContextAspect {

    /**
     * 环绕通知：在方法执行前设置上下文，执行后自动清理。
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(ignoreTenantContext)")
    public Object aroundIgnoreTenantContext(ProceedingJoinPoint joinPoint,
                                            IgnoreTenantContext ignoreTenantContext) throws Throwable {
        if (TenantContextHolder.current().isEmpty()) {
            // 创建忽略租户上下文的上下文对象
            TenantThreadContext context = new TenantThreadContext(null, null, null, true);
            // 打开上下文作用域，执行方法，然后自动清理
            try (TenantContextHolder.TenantContextScope scope = TenantContextHolder.open(context)) {
                return joinPoint.proceed();
            }
        }
        return joinPoint.proceed();
    }
}

