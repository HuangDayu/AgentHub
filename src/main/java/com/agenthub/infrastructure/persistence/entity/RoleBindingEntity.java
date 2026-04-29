package com.agenthub.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * 角色绑定持久化实体。
 * 对应数据库表 app.role_binding。
 */
@TableName("app.role_binding")
public class RoleBindingEntity {
    /** 绑定ID（主键） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 用户ID */
    private String userId;
    /** 角色ID */
    private String roleId;
    /** 创建时间 */
    private Instant createdAt;

    /** 获取ID。 */
    public String getId() { return id; }
    /** 设置ID。 */
    public void setId(String id) { this.id = id; }

    /** 获取用户ID。 */
    public String getUserId() { return userId; }
    /** 设置用户ID。 */
    public void setUserId(String userId) { this.userId = userId; }

    /** 获取角色ID。 */
    public String getRoleId() { return roleId; }
    /** 设置角色ID。 */
    public void setRoleId(String roleId) { this.roleId = roleId; }

    /** 获取创建时间。 */
    public Instant getCreatedAt() { return createdAt; }
    /** 设置创建时间。 */
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
