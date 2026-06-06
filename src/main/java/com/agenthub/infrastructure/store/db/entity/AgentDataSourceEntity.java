package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * Agent 数据源实体
 */
@Data
@TableName("agent_data_source")
public class AgentDataSourceEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String description;
    private String protocol;
    private String endpointUri;
    private String propertiesJson;
    private Boolean enabled;
    private String status;
    private String lastErrorMessage;
    private Instant lastCheckedAt;
    private String permissionPolicyId;
    private String schemaId;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private Instant createdAt;
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
