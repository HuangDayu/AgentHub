package com.agenthub.infrastructure.context;

/**
 * 租户上下文提供者接口。
 * <p>
 * 定义获取租户ID的契约。
 * </p>
 */
public interface TenantContextSupplier {
    
    TenantThreadContext getTenantThreadContext();

}
