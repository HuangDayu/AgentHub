package com.agenthub.infrastructure.context;

/**
 * 租户线程上下文提供者。
 * <p>
 * 从线程上下文或HTTP请求头中获取租户ID。
 * </p>
 */
public class TenantThreadContextSupplier implements TenantContextSupplier {


    @Override
    public TenantThreadContext getTenantThreadContext() {
        return TenantContextHolder.current().get();
    }

}
