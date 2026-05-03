package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

}
