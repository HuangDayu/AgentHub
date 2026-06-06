package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 权限策略实体
 */
@Data
@TableName("permission_strategy")
public class PermissionStrategyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String allowedRoles;
    private String allowedOperations;
    private String protocolBlocklist;
    private Boolean dangerousSqlBlock;
    private String requireApprovalFor;
    private String tablePermissions;
    private Integer rateLimitPerMinute;
    private Integer rateLimitPerHour;
    private Boolean auditLogEnabled;
    private Integer auditLogRetentionDays;
    private Boolean piiMaskingOnResult;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private Instant createdAt;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
