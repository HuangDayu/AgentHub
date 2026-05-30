package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.TOOL;
import static com.agenthub.domain.enums.AgentConfigType.SKILL_TOOL;

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
    @TableField(value = "description", jdbcType = JdbcType.LONGVARCHAR)
    private String description;
    @TableField(value = "skill_type")
    private String skillType;
    @TableField(value = "skill_path", jdbcType = JdbcType.LONGVARCHAR)
    private String skillPath;
    @TableField(value = "skill_files_tree", jdbcType = JdbcType.LONGVARCHAR)
    private String skillFilesTree;
    private boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
