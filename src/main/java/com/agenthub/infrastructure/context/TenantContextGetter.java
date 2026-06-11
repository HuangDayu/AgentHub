package com.agenthub.infrastructure.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
        return getTenantThreadContext().getTenantId();
    }


    public String getWorkspaceId() {
        return getTenantThreadContext().getWorkspaceId();
    }


    public boolean isIgnoreTenantContext() {
        return getTenantThreadContext().isIgnoreTenantContext();
    }

    public String getRequestId() {
        return getTenantThreadContext().getRequestId();
    }

    public Optional<TenantThreadContext> findTenantThreadContext() {
        try {
            return Optional.ofNullable(getTenantThreadContext());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 查找第一个有效的上下文数据对象
     */
    public TenantThreadContext getTenantThreadContext() {
        return tenantContextSupplier.stream()
                .flatMap(this::safeGetContext)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid tenant context found"));
    }

    private Stream<TenantThreadContext> safeGetContext(TenantContextSupplier supplier) {
        try {
            return Stream.ofNullable(supplier.getTenantThreadContext());
        } catch (Exception e) {
            log.warn("Failed to get tenant context from supplier: {} , error message: {}", supplier, e.getMessage());
            return Stream.empty();
        }
    }
}
