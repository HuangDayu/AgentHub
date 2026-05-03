package com.agenthub.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * 工作区持久化实体。
 * 对应数据库表 app.workspace。
 */
@TableName("app.workspace")
public class WorkspaceEntity {
    @TableId(type = IdType.INPUT)
    private String id;
    private String tenantId;
    private String workspaceCode;
    private String name;
    private String region;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    /** 获取ID。 */
    public String getId() { return id; }
    /** 设置ID。 */
    public void setId(String id) { this.id = id; }
    /** 获取租户ID。 */
    public String getTenantId() { return tenantId; }
    /** 设置租户ID。 */
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    /** 获取工作区代码。 */
    public String getWorkspaceCode() { return workspaceCode; }
    /** 设置工作区代码。 */
    public void setWorkspaceCode(String workspaceCode) { this.workspaceCode = workspaceCode; }
    /** 获取名称。 */
    public String getName() { return name; }
    /** 设置名称。 */
    public void setName(String name) { this.name = name; }
    /** 获取区域。 */
    public String getRegion() { return region; }
    /** 设置区域。 */
    public void setRegion(String region) { this.region = region; }
    /** 获取状态。 */
    public String getStatus() { return status; }
    /** 设置状态。 */
    public void setStatus(String status) { this.status = status; }
    /** 获取创建时间。 */
    public Instant getCreatedAt() { return createdAt; }
    /** 设置创建时间。 */
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    /** 获取更新时间。 */
    public Instant getUpdatedAt() { return updatedAt; }
    /** 设置更新时间。 */
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
