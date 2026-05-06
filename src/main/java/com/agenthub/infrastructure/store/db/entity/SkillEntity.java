package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("app.skill")
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
    private String definition;
    private String parameters;
    private boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;

}
