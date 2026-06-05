package com.agenthub.infrastructure.context;


import com.agenthub.common.annotations.IgnoreTenantContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import static com.agenthub.common.constants.AgentConstants.THREAD_CONTEXT_KEY;

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

            // 打开上下文作用域，执行方法，然后自动清理
            try (TenantContextHolder.TenantContextScope scope = TenantContextHolder.open(TenantThreadContext.ignoreContext())) {
                return joinPoint.proceed();
            }
        }
        return joinPoint.proceed();
    }


    @Around("@annotation(tool)")
    public Object openToolsTenantContext(ProceedingJoinPoint joinPoint, Tool tool) throws Throwable {
        if (TenantContextHolder.current().isPresent()) {
            return joinPoint.proceed();
        }
        return proceedWithInjectedTenant(joinPoint);
    }

    private Object proceedWithInjectedTenant(ProceedingJoinPoint joinPoint) throws Throwable {
        TenantThreadContext context = findTenantContext(joinPoint);
        if (context == null) return joinPoint.proceed();
        try (TenantContextHolder.TenantContextScope scope = TenantContextHolder.open(context)) {
            return joinPoint.proceed();
        }
    }

    private TenantThreadContext findTenantContext(ProceedingJoinPoint joinPoint) {
        for (Object paramValue : joinPoint.getArgs()) {
            if (paramValue instanceof ToolContext toolContext
                    && toolContext.getContext().get(THREAD_CONTEXT_KEY) instanceof TenantThreadContext ctx) {
                return ctx;
            }
        }
        return null;
    }

}

