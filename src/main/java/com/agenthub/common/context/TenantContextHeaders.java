package com.agenthub.common.context;

/**
 * 租户上下文HTTP头常量。
 * <p>
 * 定义用于传递租户信息的HTTP头名称。
 * </p>
 */
public final class TenantContextHeaders {

    /** 租户ID头名称 */
    public static final String CONTEXT_TENANT_ID = "X-Tenant-Id";

    /** 工作空间ID头名称 */
    public static final String CONTEXT_WORKSPACE_ID = "X-Workspace-Id";

    /** 请求ID头名称 */
    public static final String CONTEXT_REQUEST_ID = "X-Request-Id";

    /**
     * 私有构造函数，防止实例化。
     */
    private TenantContextHeaders() {
    }
}
