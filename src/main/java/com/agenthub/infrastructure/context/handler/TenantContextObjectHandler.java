package com.agenthub.infrastructure.context.handler;

import cn.hutool.core.util.StrUtil;
import com.agenthub.infrastructure.context.TenantContextGetter;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
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

    public static final String TENANT_ID = "tenantId";
    public static final String WORKSPACE_ID = "workspaceId";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
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
        strictInsertFill(metaObject, CREATED_AT, Instant.class, now);
        strictInsertFill(metaObject, UPDATED_AT, Instant.class, now);
    }

    /**
     * 填充租户ID字段。
     */
    private void fillTenantId(MetaObject metaObject) {
        if (shouldFillTenantId(metaObject)) {
            String tenantId = tenantContextGetter.getTenantId();
            applyTenantIdIfValid(metaObject, tenantId);
        }
    }

    private boolean shouldFillTenantId(MetaObject metaObject) {
        if (!metaObject.hasSetter(TENANT_ID)) return false;
        Object value = getFieldValByName(TENANT_ID, metaObject);
        return value == null || StrUtil.isBlank(StrUtil.toString(value));
    }

    private void applyTenantIdIfValid(MetaObject metaObject, String tenantId) {
        if (isValidTenantId(tenantId)) {
            setFieldValByName(TENANT_ID, tenantId, metaObject);
        }
    }

    private void fillWorkspaceId(MetaObject metaObject) {
        if (shouldFillWorkspaceId(metaObject)) {
            String workspaceId = tenantContextGetter.getWorkspaceId();
            applyWorkspaceIdIfValid(metaObject, workspaceId);
        }
    }

    private boolean shouldFillWorkspaceId(MetaObject metaObject) {
        if (!metaObject.hasSetter(WORKSPACE_ID)) return false;
        Object value = getFieldValByName(WORKSPACE_ID, metaObject);
        return value == null || StrUtil.isBlank(StrUtil.toString(value));
    }

    private void applyWorkspaceIdIfValid(MetaObject metaObject, String workspaceId) {
        if (isValidTenantId(workspaceId)) {
            setFieldValByName(WORKSPACE_ID, workspaceId, metaObject);
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
