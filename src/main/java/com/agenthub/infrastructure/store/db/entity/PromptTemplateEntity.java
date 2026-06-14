package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.PromptTemplateMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.PROMPT;
import static com.agenthub.domain.enums.AgentConfigType.SYSTEM_PROMPT;

@Data
@TableName(value = "prompt_template")
@ConfigChangeListenerEntity(category = PROMPT, type = SYSTEM_PROMPT)
@AgentDataModel(
    name = "提示词模板",
    description = "Agent的提示词模板，用于管理可复用的提示词内容",
    domain = "提示词管理",
    mapper = PromptTemplateMybatisMapper.class
)
public class PromptTemplateEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @AgentDataField(description = "模板名称")
    private String name;
    @AgentDataField(description = "模板描述")
    private String description;
    @AgentDataField(description = "模板分类", filterable = true)
    private String category;
    @AgentDataField(description = "模板内容")
    private String content;
    @AgentDataField(description = "变量定义")
    private String variables;
    @AgentDataField(description = "是否激活")
    private boolean active;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
