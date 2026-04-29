package com.agenthub.common.context.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.agenthub.common.context.TenantContextGetter;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 租户上下文元对象处理器。
 * <p>
 * 自动填充创建时间、更新时间和租户ID。
 * </p>
 */
@Component
public class TenantContextObjectHandler implements MetaObjectHandler {

    /**
     * 租户上下文获取器
     */
    private final TenantContextGetter tenantContextGetter;

    /**
     * 构造函数。
     *
     * @param tenantContextGetter 租户上下文获取器
     */
    public TenantContextObjectHandler(TenantContextGetter tenantContextGetter) {
        this.tenantContextGetter = tenantContextGetter;
    }

    /**
     * 插入时自动填充。
     * <p>
     * 填充创建时间、更新时间和租户ID。
     * </p>
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Instant now = Instant.now();
        fillTimestamps(metaObject, now);
        fillTenantId(metaObject);
        fillWorkspaceId(metaObject);
    }

    /**
     * 填充时间戳字段。
     */
    private void fillTimestamps(MetaObject metaObject, Instant now) {
        strictInsertFill(metaObject, "createdAt", Instant.class, now);
        strictInsertFill(metaObject, "updatedAt", Instant.class, now);
    }

    /**
     * 填充租户ID字段。
     */
    private void fillTenantId(MetaObject metaObject) {
        String tenantId = tenantContextGetter.getTenantId();
        if (isValidTenantId(tenantId)) {
            setFieldValByName("tenantId", tenantId, metaObject);
        }
    }

    private void fillWorkspaceId(MetaObject metaObject) {
        String workspaceId = tenantContextGetter.getWorkspaceId();
        if (isValidTenantId(workspaceId)) {
            setFieldValByName("workspaceId", workspaceId, metaObject);
        }
    }

    /**
     * 判断租户ID是否有效。
     */
    private boolean isValidTenantId(String tenantId) {
        return tenantId != null && !tenantId.isBlank();
    }

    /**
     * 更新时自动填充。
     * <p>
     * 填充更新时间。
     * </p>
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", Instant.class, Instant.now());
    }
}
