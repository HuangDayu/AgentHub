package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

@Data
@TableName(value = "app.mcp_tool", autoResultMap = true)
public class McpToolEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String description;
    private String serverUrl;
    private String serverType;
    private String command;
    private String args;
    private String env;
    private boolean async;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

}
