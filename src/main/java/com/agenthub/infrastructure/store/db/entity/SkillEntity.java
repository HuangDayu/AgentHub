package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.SkillMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.TOOL;
import static com.agenthub.domain.enums.AgentConfigType.SKILL_TOOL;

@Data
@TableName("skill")
@ConfigChangeListenerEntity(category = TOOL, type = SKILL_TOOL)
@AgentDataModel(
    name = "技能",
    description = "Agent可使用的技能配置，包括技能路径、文件和来源信息",
    domain = "技能管理",
    mapper = SkillMybatisMapper.class
)
public class SkillEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @AgentDataField(description = "技能唯一编码")
    @TableField(value = "skill_code")
    private String skillCode;
    @AgentDataField(description = "技能名称")
    private String name;
    @AgentDataField(description = "技能描述")
    @TableField(value = "description", jdbcType = JdbcType.LONGVARCHAR)
    private String description;
    @AgentDataField(description = "技能类型", filterable = true)
    @TableField(value = "skill_type")
    private String skillType;
    @AgentDataField(description = "技能路径")
    @TableField(value = "skill_path", jdbcType = JdbcType.LONGVARCHAR)
    private String skillPath;
    @AgentDataField(description = "技能文件树")
    @TableField(value = "skill_files_tree", jdbcType = JdbcType.LONGVARCHAR)
    private String skillFilesTree;
    @AgentDataField(description = "技能来源")
    @TableField(value = "source")
    private String source;
    @AgentDataField(description = "来源路径")
    @TableField(value = "source_path", jdbcType = JdbcType.LONGVARCHAR)
    private String sourcePath;
    @AgentDataField(description = "ZIP存储路径")
    @TableField(value = "zip_storage_path", jdbcType = JdbcType.LONGVARCHAR)
    private String zipStoragePath;
    @AgentDataField(description = "关联配置ID")
    @TableField(value = "config_id")
    private String configId;
    @AgentDataField(description = "文件数量")
    @TableField(value = "file_count")
    private int fileCount;
    @AgentDataField(description = "总大小(字节)")
    @TableField(value = "total_size")
    private long totalSize;
    @AgentDataField(description = "是否启用")
    private boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
    @AgentDataField(description = "最后同步时间")
    @TableField(value = "last_sync_at")
    private Instant lastSyncAt;
}
