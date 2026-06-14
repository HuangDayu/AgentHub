package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.PermissionStrategyMybatisMapper;
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
@AgentDataModel(
    name = "权限策略",
    description = "权限策略配置，管理访问控制与审计日志",
    domain = "策略管理",
    mapper = PermissionStrategyMybatisMapper.class
)
public class PermissionStrategyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @AgentDataField(hidden = true)
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private String tenantId;

    @AgentDataField(hidden = true)
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private String workspaceId;

    @AgentDataField(description = "策略名称", filterable = true)
    private String name;

    @AgentDataField(description = "策略描述")
    private String description;

    @AgentDataField(description = "允许角色")
    private String allowedRoles;

    @AgentDataField(description = "允许操作")
    private String allowedOperations;

    @AgentDataField(description = "协议黑名单")
    private String protocolBlocklist;

    @AgentDataField(description = "是否拦截危险SQL")
    private Boolean dangerousSqlBlock;

    @AgentDataField(description = "需要审批的操作")
    private String requireApprovalFor;

    @AgentDataField(description = "表级权限")
    private String tablePermissions;

    @AgentDataField(description = "每分钟速率限制")
    private Integer rateLimitPerMinute;

    @AgentDataField(description = "每小时速率限制")
    private Integer rateLimitPerHour;

    @AgentDataField(description = "是否启用审计日志")
    private Boolean auditLogEnabled;

    @AgentDataField(description = "审计日志保留天数")
    private Integer auditLogRetentionDays;

    @AgentDataField(description = "结果PII脱敏")
    private Boolean piiMaskingOnResult;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private Instant createdAt;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
