package com.agenthub.infrastructure.store.db.entity;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

import static com.agenthub.domain.model.AgentConfigCategory.TOOL;
import static com.agenthub.domain.model.AgentConfigType.HTTP_TOOL;
import static com.agenthub.domain.model.AgentConfigType.SKILL_TOOL;

@Data
@TableName("skill")
@ConfigChangeListenerEntity(category = TOOL, type = SKILL_TOOL)
public class SkillEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @TableField(value = "skill_code")
    private String skillCode;
    private String name;
    private String description;
    @TableField(value = "skill_type")
    private String skillType;
    @TableField(value = "skill_path")
    private String skillPath;
    @TableField(value = "skill_files_tree")
    private String skillFilesTree;
    private boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;
}
