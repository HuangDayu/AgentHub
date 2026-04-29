package com.agenthub.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 租户领域模型.
 * <p>
 * 表示系统中的租户实体，包含租户的基本信息和业务操作方法。
 * 采用不可变设计，所有状态变更操作都返回新的实例。
 * </p>
 */
public record Tenant(
        /** 租户ID */ String id,
        /** 租户编码 */ String tenantCode,
        /** 租户名称 */ String name,
        /** 套餐编码 */ String planCode,
        /** 隔离级别 */ IsolationLevel isolationLevel,
        /** 租户状态 */ TenantStatus status,
        /** 区域 */ String region,
        /** 创建时间 */ Instant createdAt,
        /** 更新时间 */ Instant updatedAt
) {
    /**
     * 紧凑构造函数，验证租户字段的合法性。
     *
     * @throws IllegalArgumentException 当必填字段为空或空白时抛出
     * @throws NullPointerException     当必填字段为null时抛出
     */
    public Tenant {
        validateId(id);
        tenantCode = validateNotBlank(tenantCode, "tenantCode");
        name = validateNotBlank(name, "name");
        planCode = validateNotBlank(planCode, "planCode");
        Objects.requireNonNull(isolationLevel, "isolationLevel must not be null");
        Objects.requireNonNull(status, "status must not be null");
        region = validateNotBlank(region, "region");
    }

    /**
     * 创建新的租户实例，自动生成ID。
     *
     * @param tenantCode     租户编码
     * @param name           租户名称
     * @param planCode       套餐编码
     * @param isolationLevel 隔离级别
     * @param region         区域
     * @param now            当前时间
     * @return 新创建的租户实例
     */
    public static Tenant create(
            String tenantCode, String name, String planCode,
            IsolationLevel isolationLevel, String region, Instant now
    ) {
        return createWithId(
                UUID.randomUUID().toString(), tenantCode, name,
                planCode, isolationLevel, region, now
        );
    }

    /**
     * 使用指定ID创建新的租户实例。
     *
     * @param id             租户ID
     * @param tenantCode     租户编码
     * @param name           租户名称
     * @param planCode       套餐编码
     * @param isolationLevel 隔离级别
     * @param region         区域
     * @param now            当前时间
     * @return 新创建的租户实例
     * @throws NullPointerException 当now为null时抛出
     */
    public static Tenant createWithId(
            String id, String tenantCode, String name, String planCode,
            IsolationLevel isolationLevel, String region, Instant now
    ) {
        Objects.requireNonNull(now, "now must not be null");
        return new Tenant(
                id, tenantCode, name, planCode,
                isolationLevel, TenantStatus.ACTIVE, region, now, now
        );
    }

    /**
     * 从持久化数据重建租户实例。
     *
     * @param id             租户ID
     * @param tenantCode     租户编码
     * @param name           租户名称
     * @param planCode       套餐编码
     * @param isolationLevel 隔离级别
     * @param status         租户状态
     * @param region         区域
     * @param createdAt      创建时间
     * @param updatedAt      更新时间
     * @return 重建的租户实例
     */
    public static Tenant rehydrate(
            String id, String tenantCode, String name, String planCode,
            IsolationLevel isolationLevel, TenantStatus status, String region,
            Instant createdAt, Instant updatedAt
    ) {
        return new Tenant(
                id, tenantCode, name, planCode,
                isolationLevel, status, region, createdAt, updatedAt
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
     * 重命名租户。
     *
     * @param newName   新的租户名称
     * @param updatedAt 更新时间
     * @return 更新后的租户实例
     */
    public Tenant rename(String newName, Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new Tenant(
                id, tenantCode, newName, planCode,
                isolationLevel, status, region, createdAt, updatedAt
        );
    }

    /**
     * 更改租户套餐。
     *
     * @param newPlanCode 新的套餐编码
     * @param updatedAt   更新时间
     * @return 更新后的租户实例
     */
    public Tenant changePlan(String newPlanCode, Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new Tenant(
                id, tenantCode, name, newPlanCode,
                isolationLevel, status, region, createdAt, updatedAt
        );
    }

    /**
     * 挂起租户。
     *
     * @param updatedAt 更新时间
     * @return 状态为SUSPENDED的租户实例
     */
    public Tenant suspend(Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new Tenant(
                id, tenantCode, name, planCode,
                isolationLevel, TenantStatus.SUSPENDED, region, createdAt, updatedAt
        );
    }

    /**
     * 激活租户。
     *
     * @param updatedAt 更新时间
     * @return 状态为ACTIVE的租户实例
     */
    public Tenant activate(Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new Tenant(
                id, tenantCode, name, planCode,
                isolationLevel, TenantStatus.ACTIVE, region, createdAt, updatedAt
        );
    }

    /**
     * 隔离级别枚举.
     */
    public enum IsolationLevel {L1, L2, L3}

    /**
     * 租户状态枚举.
     */
    public enum TenantStatus {ACTIVE, SUSPENDED, DELETED}
}
