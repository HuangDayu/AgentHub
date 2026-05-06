package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 应用用户持久化实体。
 * 对应数据库表 app.app_user。
 */
@Data
@TableName("app.app_user")
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
    private Instant createdAt;
    /**
     * 更新时间
     */
    private Instant updatedAt;


}
