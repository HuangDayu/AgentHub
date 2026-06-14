package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;
import com.agenthub.infrastructure.store.db.mapper.AgentConfigMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("agent_config")
@AgentDataModel(
    name = "Agent配置",
    description = "智能体配置绑定，关联Agent与各类配置项",
    domain = "Agent管理",
    mapper = AgentConfigMybatisMapper.class
)
public class AgentConfigEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(description = "Agent ID", filterable = true)
    private String agentId;
    @AgentDataField(description = "配置分类", filterable = true, enumType = AgentConfigCategory.class)
    private String category;
    @AgentDataField(description = "配置类型", filterable = true, enumType = AgentConfigType.class)
    private String type;
    @AgentDataField(description = "配置ID")
    private String configId;
    @AgentDataField(description = "配置名称")
    private String name;
    @AgentDataField(description = "配置描述")
    private String description;
    @AgentDataField(description = "优先级")
    private Integer priority;
    @AgentDataField(description = "是否启用")
    private boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
