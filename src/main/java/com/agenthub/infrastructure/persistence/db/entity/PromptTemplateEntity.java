package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName(value = "app.prompt_template", autoResultMap = true)
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
