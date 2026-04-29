package com.agenthub.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 工作空间领域模型.
 * <p>
 * 表示租户下的工作空间实体，包含工作空间的基本信息和业务操作方法。
 * 采用不可变设计，所有状态变更操作都返回新的实例。
 * </p>
 */
public record Workspace(
        /** 工作空间ID */ String id,
        /** 租户ID */ String tenantId,
        /** 工作空间编码 */ String workspaceCode,
        /** 工作空间名称 */ String name,
        /** 区域 */ String region,
        /** 工作空间状态 */ WorkspaceStatus status,
        /** 创建时间 */ Instant createdAt,
        /** 更新时间 */ Instant updatedAt
) {
    /**
     * 紧凑构造函数，验证工作空间字段的合法性。
     *
     * @throws IllegalArgumentException 当必填字段为空或空白时抛出
     * @throws NullPointerException     当必填字段为null时抛出
     */
    public Workspace {
        validateId(id);
        tenantId = validateNotBlank(tenantId, "tenantId");
        workspaceCode = validateNotBlank(workspaceCode, "workspaceCode");
        name = validateNotBlank(name, "name");
        region = validateNotBlank(region, "region");
        Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * 创建新的工作空间实例，自动生成ID。
     *
     * @param tenantId      租户ID
     * @param workspaceCode 工作空间编码
     * @param name          工作空间名称
     * @param region        区域
     * @param now           当前时间
     * @return 新创建的工作空间实例
     */
    public static Workspace create(
            String tenantId, String workspaceCode, String name, String region, Instant now
    ) {
        return createWithId(
                UUID.randomUUID().toString(), tenantId, workspaceCode, name, region, now
        );
    }

    /**
     * 使用指定ID创建新的工作空间实例。
     *
     * @param id            工作空间ID
     * @param tenantId      租户ID
     * @param workspaceCode 工作空间编码
     * @param name          工作空间名称
     * @param region        区域
     * @param now           当前时间
     * @return 新创建的工作空间实例
     * @throws NullPointerException 当now为null时抛出
     */
    public static Workspace createWithId(
            String id, String tenantId, String workspaceCode, String name, String region, Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");
        return new Workspace(
                id, tenantId, workspaceCode, name, region,
                WorkspaceStatus.ACTIVE, now, now
        );
    }

    /**
     * 从持久化数据重建工作空间实例。
     *
     * @param id            工作空间ID
     * @param tenantId      租户ID
     * @param workspaceCode 工作空间编码
     * @param name          工作空间名称
     * @param region        区域
     * @param status        工作空间状态
     * @param createdAt     创建时间
     * @param updatedAt     更新时间
     * @return 重建的工作空间实例
     */
    public static Workspace rehydrate(
            String id, String tenantId, String workspaceCode, String name,
            String region, WorkspaceStatus status, Instant createdAt, Instant updatedAt
    ) {
        return new Workspace(
                id, tenantId, workspaceCode, name, region, status, createdAt, updatedAt
        );
    }

    /**
     * 验证ID不为空。
     *
     * @param id 待验证的ID
     * @throws IllegalArgumentException 当ID为null或空白时抛出
     */
    private static void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }

    /**
     * 验证字符串字段不为空白。
     *
     * @param value     待验证的值
     * @param fieldName 字段名称
     * @return 去除首尾空白后的值
     * @throws IllegalArgumentException 当值为null或空白时抛出
     */
    private static String validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    /**
     * 重命名工作空间。
     *
     * @param newName   新的工作空间名称
     * @param updatedAt 更新时间
     * @return 更新后的工作空间实例
     */
    public Workspace rename(String newName, Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new Workspace(
                id, tenantId, workspaceCode, newName, region, status, createdAt, updatedAt
        );
    }

    /**
     * 挂起工作空间。
     *
     * @param updatedAt 更新时间
     * @return 状态为SUSPENDED的工作空间实例
     */
    public Workspace suspend(Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new Workspace(
                id, tenantId, workspaceCode, name, region, WorkspaceStatus.SUSPENDED, createdAt, updatedAt
        );
    }

    /**
     * 激活工作空间。
     *
     * @param updatedAt 更新时间
     * @return 状态为ACTIVE的工作空间实例
     */
    public Workspace activate(Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new Workspace(
                id, tenantId, workspaceCode, name, region, WorkspaceStatus.ACTIVE, createdAt, updatedAt
        );
    }

    /**
     * 工作空间状态枚举.
     */
    public enum WorkspaceStatus {ACTIVE, SUSPENDED}
}
