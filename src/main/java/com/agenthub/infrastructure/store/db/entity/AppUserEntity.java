package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 应用用户持久化实体。
 * 对应数据库表 app_user。
 */
@Data
@TableName("app_user")
public class AppUserEntity {
    /**
     * 用户ID（主键）
     */
    @TableId(type = IdType.INPUT)
    private String id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码哈希
     */
    private String passwordHash;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 用户状态
     */
    private String status;

    private String email;
    private String displayName;
    private String authSource;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;


}
