package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.McpServerType;
import com.agenthub.infrastructure.store.db.mapper.McpToolMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.TOOL;
import static com.agenthub.domain.enums.AgentConfigType.MCP_TOOL;

@Data
@TableName(value = "mcp_tool", autoResultMap = true)
@ConfigChangeListenerEntity(category = TOOL, type = MCP_TOOL)
@AgentDataModel(
    name = "MCP工具",
    description = "MCP协议工具，通过Model Context Protocol调用外部服务",
    domain = "工具管理",
    mapper = McpToolMybatisMapper.class
)
public class McpToolEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @AgentDataField(description = "工具名称", required = true, filterable = true)
    private String name;
    @AgentDataField(description = "工具描述")
    private String description;
    @AgentDataField(description = "服务地址")
    private String serverUrl;
    @AgentDataField(description = "服务类型", enumType = McpServerType.class)
    private String serverType;
    @AgentDataField(description = "启动命令")
    private String command;
    @AgentDataField(description = "命令参数")
    private String args;
    @AgentDataField(description = "环境变量")
    private String env;
    @AgentDataField(description = "是否异步")
    private boolean async;
    @AgentDataField(description = "是否启用")
    private boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

}
