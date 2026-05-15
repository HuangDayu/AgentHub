package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * 租户持久化实体。
 * 对应数据库表 tenant。
 */
@Data
@TableName("tenant")
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


}
