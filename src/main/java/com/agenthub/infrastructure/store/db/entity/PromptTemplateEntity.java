package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

import static com.agenthub.domain.model.AgentConfigCategory.PROMPT;
import static com.agenthub.domain.model.AgentConfigType.SYSTEM_PROMPT;

@Data
@TableName(value = "app.prompt_template", autoResultMap = true)
@ConfigChangeListenerEntity(category = PROMPT, type = SYSTEM_PROMPT)
public class PromptTemplateEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String description;
    private String category;
    private String content;
    private String variables;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;


}
