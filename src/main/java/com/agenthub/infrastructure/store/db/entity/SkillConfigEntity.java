package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * 技能配置实体。
 */
@Data
@TableName("skill_config")
public class SkillConfigEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    private String name;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String description;

    @TableField(value = "skill_paths", jdbcType = JdbcType.LONGVARCHAR)
    private String skillPaths;

    @TableField(value = "sync_enabled")
    private Boolean syncEnabled;

    @TableField(value = "sync_interval")
    private Integer syncInterval;

    @TableField(value = "auto_sync")
    private Boolean autoSync;

    private Boolean enabled;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
