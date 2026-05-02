package com.agenthub.infrastructure.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 租户上下文获取器。
 * <p>
 * 从多个租户上下文提供者中获取租户ID。
 * </p>
 */
public class TenantContextGetter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextGetter.class);
    /**
     * 租户上下文提供者列表
     */
    private final List<TenantContextSupplier> tenantContextSupplier;

    /**
     * 构造函数。
     *
     * @param tenantContextSupplier 租户上下文提供者列表
     */
    public TenantContextGetter(List<TenantContextSupplier> tenantContextSupplier) {
        this.tenantContextSupplier = tenantContextSupplier;
    }

    /**
     * 获取当前租户ID。
     * <p>
     * 遍历所有提供者，返回第一个有效的租户ID。
     * </p>
     *
     * @return 租户ID，若无则返回null
     */
    public String getTenantId() {
        return findFirstTenantContext().tenantId();
    }


    public String getWorkspaceId() {
        return findFirstTenantContext().workspaceId();
    }


    public boolean isIgnoreTenantContext() {
        return findFirstTenantContext().ignoreTenantContext();
    }

    public String getRequestId() {
        return findFirstTenantContext().requestId();
    }

    /**
     * 查找第一个有效的上下文数据对象
     */
    private TenantThreadContext findFirstTenantContext() {
        for (TenantContextSupplier supplier : tenantContextSupplier) {
            try {
                TenantThreadContext tenantThreadContext = supplier.getTenantThreadContext();
                if (tenantThreadContext != null) {
                    return tenantThreadContext;
                }
            } catch (Exception e) {
                log.warn("Failed to get tenant context from supplier: {}", supplier, e);
            }
        }
        throw new IllegalStateException("No valid tenant context found");
    }
}
