package com.agenthub.infrastructure.context;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * 租户MDC上下文。
 * <p>
 * 将租户信息放入SLF4J MDC中用于日志记录。
 * </p>
 */
public final class TenantMdcContext {

    /**
     * 私有构造函数，防止实例化。
     */
    private TenantMdcContext() {
    }

    /**
     * 应用租户上下文到MDC。
     *
     * @param context 租户线程上下文
     */
    public static void apply(TenantThreadContext context) {
        putIfPresent("tenant_id", context.getTenantId());
        putIfPresent("workspace_id", context.getWorkspaceId());
        putIfPresent("trace_id", context.getRequestId());
    }

    /**
     * 清除MDC中的租户信息。
     */
    public static void clear() {
        MDC.remove("tenant_id");
        MDC.remove("workspace_id");
        MDC.remove("trace_id");
    }

    /**
     * 如果值存在则放入MDC。
     */
    private static void putIfPresent(String key, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        MDC.put(key, value);
    }
}
