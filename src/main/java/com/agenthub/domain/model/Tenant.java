package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 租户领域模型.
 * <p>
 * 表示系统中的租户实体，包含租户的基本信息和业务操作方法。
 * 采用不可变设计，所有状态变更操作都返回新的实例。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    /**
     * 租户ID
     */
    private String id;
    /**
     * 租户编码
     */
    private String tenantCode;
    /**
     * 租户名称
     */
    private String name;
    /**
     * 套餐编码
     */
    private String planCode;
    /**
     * 隔离级别
     */
    private IsolationLevel isolationLevel;
    /**
     * 区域
     */
    private String region;
    private TenantStatus status;
    /**
     * 创建时间
     */
    private Instant createdAt;
    /**
     * 更新时间
     */
    private Instant updatedAt;


    /**
     * 隔离级别枚举.
     */
    public enum IsolationLevel {L1, L2, L3}

    /**
     * 租户状态枚举.
     */
    public enum TenantStatus {ACTIVE, SUSPENDED, DELETED}
}
