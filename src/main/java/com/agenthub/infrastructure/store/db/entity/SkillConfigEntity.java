package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.SkillConfigMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * 技能配置实体。
 */
@Data
@TableName("skill_config")
@AgentDataModel(
    name = "技能配置",
    description = "技能的同步和管理配置，包括路径、同步间隔和自动同步设置",
    domain = "技能管理",
    mapper = SkillConfigMybatisMapper.class
)
public class SkillConfigEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @AgentDataField(description = "配置名称")
    private String name;

    @AgentDataField(description = "配置描述")
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String description;

    @AgentDataField(description = "技能路径列表")
    @TableField(value = "skill_paths", jdbcType = JdbcType.LONGVARCHAR)
    private String skillPaths;

    @AgentDataField(description = "是否启用同步")
    @TableField(value = "sync_enabled")
    private Boolean syncEnabled;

    @AgentDataField(description = "同步间隔(分钟)")
    @TableField(value = "sync_interval")
    private Integer syncInterval;

    @AgentDataField(description = "是否自动同步")
    @TableField(value = "auto_sync")
    private Boolean autoSync;

    @AgentDataField(description = "是否启用")
    private Boolean enabled;

    @AgentDataField(description = "最后同步时间")
    @TableField(value = "last_sync_at")
    private Instant lastSyncAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
