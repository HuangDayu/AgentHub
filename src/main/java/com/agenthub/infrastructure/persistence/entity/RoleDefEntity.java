package com.agenthub.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * 角色定义持久化实体。
 * 对应数据库表 app.role_def。
 */
@TableName("app.role_def")
public class RoleDefEntity {
    /** 角色ID（主键） */
    @TableId(type = IdType.INPUT)
    private String id;
    /** 角色编码 */
    private String roleCode;
    /** 角色名称 */
    private String roleName;
    private String roleType;
    /** 描述 */
    private String description;
    /** 创建时间 */
    private Instant createdAt;

    /** 获取ID。 */
    public String getId() { return id; }
    /** 设置ID。 */
    public void setId(String id) { this.id = id; }

    /** 获取角色编码。 */
    public String getRoleCode() { return roleCode; }
    /** 设置角色编码。 */
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    /** 获取角色名称。 */
    public String getRoleName() { return roleName; }
    /** 设置角色名称。 */
    public void setRoleName(String roleName) { this.roleName = roleName; }

    /** 获取描述。 */
    public String getDescription() { return description; }
    /** 设置描述。 */
    public void setDescription(String description) { this.description = description; }

    /** 获取创建时间。 */
    public Instant getCreatedAt() { return createdAt; }
    /** 设置创建时间。 */
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }
}
