package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.AgentRuntimeCategory;
import com.agenthub.domain.enums.AgentStatus;
import com.agenthub.domain.enums.AgentType;
import com.agenthub.infrastructure.store.db.mapper.AgentMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("agent")
@AgentDataModel(
    name = "Agent",
    description = "智能体配置，支持对话、任务执行和工具调用",
    domain = "Agent管理",
    mapper = AgentMybatisMapper.class
)
public class AgentEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    
    @AgentDataField(description = "Agent唯一标识")
    private String agentCode;
    
    @AgentDataField(description = "Agent名称", required = true, filterable = true)
    private String name;
    
    @AgentDataField(description = "Agent描述")
    private String description;
    
    @AgentDataField(description = "Agent状态", filterable = true, enumType = AgentStatus.class)
    private String status;
    
    @AgentDataField(description = "是否启用")
    private boolean enabled;

    @AgentDataField(description = "Agent类型", filterable = true, enumType = AgentType.class)
    private String type;

    @AgentDataField(description = "Agent运行时类别", filterable = true, enumType = AgentRuntimeCategory.class)
    @TableField(value = "runtime_category")
    private String runtimeCategory;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(value = "updated_by", fill = FieldFill.INSERT)
    private String updatedBy;
}
