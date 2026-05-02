package com.agenthub.infrastructure.context;

/**
 * 租户线程上下文。
 * <p>
 * 存储当前线程的租户相关信息。
 * </p>
 *
 * @param tenantId    租户ID
 * @param workspaceId 工作空间ID
 * @param requestId   请求ID
 */
public record TenantThreadContext(String tenantId, String workspaceId, String requestId, boolean ignoreTenantContext) {

    /**
     * 创建租户线程上下文实例。
     *
     * @param tenantId    租户ID
     * @param workspaceId 工作空间ID
     * @param requestId   请求ID
     * @return 租户线程上下文实例
     */
    public static TenantThreadContext from(String tenantId, String workspaceId, String requestId) {
        return new TenantThreadContext(tenantId, workspaceId, requestId, false);
    }

    public static TenantThreadContext from(String tenantId, String workspaceId, String requestId, boolean ignoreTenantContext) {
        return new TenantThreadContext(tenantId, workspaceId, requestId, ignoreTenantContext);
    }
}
