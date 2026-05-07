package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("app.system_tools")
public class SystemEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    private String toolClassName;
    private String toolName;
    private String description;
    private String category;
    private int methodCount;
    private boolean enabled;
    private boolean systemTool;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;
}
