package com.agenthub.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * 租户持久化实体。
 * 对应数据库表 app.tenant。
 */
@TableName("app.tenant")
public class TenantEntity {
    /** 租户ID（主键） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 租户编码 */
    private String tenantCode;
    /** 租户名称 */
    private String name;
    /** 套餐编码 */
    private String planCode;
    /** 隔离级别 */
    private String isolationLevel;
    /** 租户状态 */
    private String status;
    /** 区域 */
    private String region;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    /** 获取ID。 */
    public String getId() { return id; }
    /** 设置ID。 */
    public void setId(String id) { this.id = id; }

    /** 获取租户编码。 */
    public String getTenantCode() { return tenantCode; }
    /** 设置租户编码。 */
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }

    /** 获取名称。 */
    public String getName() { return name; }
    /** 设置名称。 */
    public void setName(String name) { this.name = name; }

    /** 获取套餐编码。 */
    public String getPlanCode() { return planCode; }
    /** 设置套餐编码。 */
    public void setPlanCode(String planCode) { this.planCode = planCode; }

    /** 获取隔离级别。 */
    public String getIsolationLevel() { return isolationLevel; }
    /** 设置隔离级别。 */
    public void setIsolationLevel(String isolationLevel) { this.isolationLevel = isolationLevel; }

    /** 获取状态。 */
    public String getStatus() { return status; }
    /** 设置状态。 */
    public void setStatus(String status) { this.status = status; }

    /** 获取区域。 */
    public String getRegion() { return region; }
    /** 设置区域。 */
    public void setRegion(String region) { this.region = region; }

    /** 获取创建时间。 */
    public Instant getCreatedAt() { return createdAt; }
    /** 设置创建时间。 */
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    /** 获取更新时间。 */
    public Instant getUpdatedAt() { return updatedAt; }
    /** 设置更新时间。 */
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
