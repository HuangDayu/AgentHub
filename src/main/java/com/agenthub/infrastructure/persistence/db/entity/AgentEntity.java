package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

@Data
@TableName("app.agent")
public class AgentEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    private String agentCode;
    private String name;
    private String description;
    private String status;
    private boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(value = "updated_by", fill = FieldFill.INSERT)
    private String updatedBy;
}
