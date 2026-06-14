package com.agenthub.infrastructure.store.db.entity;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import lombok.Data;

import com.agenthub.infrastructure.store.db.mapper.SystemToolsMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.TOOL;
import static com.agenthub.domain.enums.AgentConfigType.SYSTEM_TOOL;

@Data
@TableName("system_tools")
@ConfigChangeListenerEntity(category = TOOL, type = SYSTEM_TOOL)
@AgentDataModel(
    name = "系统工具",
    description = "系统内置工具，提供代码执行、文件操作等基础能力",
    domain = "工具管理",
    mapper = SystemToolsMybatisMapper.class
)
public class SystemToolsEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @AgentDataField(description = "工具类名")
    private String toolClassName;
    @AgentDataField(description = "工具名称", filterable = true)
    private String toolName;
    @AgentDataField(description = "工具描述")
    private String description;
    @AgentDataField(description = "工具分类", filterable = true)
    private String category;
    @AgentDataField(description = "方法数量")
    private int methodCount;
    @AgentDataField(description = "是否启用")
    private boolean enabled;
    @AgentDataField(description = "是否系统工具")
    private boolean systemTool;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
