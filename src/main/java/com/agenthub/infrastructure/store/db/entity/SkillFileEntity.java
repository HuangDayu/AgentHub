package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.SkillFileMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * 技能文件实体。
 */
@Data
@TableName("skill_file")
@AgentDataModel(
    name = "技能文件",
    description = "技能的文件信息，包括路径、大小、类型和存储信息",
    domain = "技能管理",
    mapper = SkillFileMybatisMapper.class
)
public class SkillFileEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @AgentDataField(description = "关联技能ID")
    @TableField(value = "skill_id")
    private String skillId;

    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @AgentDataField(description = "文件路径")
    @TableField(value = "file_path")
    private String filePath;

    @AgentDataField(description = "文件名称")
    @TableField(value = "file_name")
    private String fileName;

    @AgentDataField(description = "文件扩展名")
    @TableField(value = "file_ext")
    private String fileExt;

    @AgentDataField(description = "文件大小(字节)")
    @TableField(value = "file_size")
    private Long fileSize;

    @AgentDataField(description = "文件类型")
    @TableField(value = "file_type")
    private String fileType;

    @AgentDataField(description = "文件编码")
    private String encoding;

    @AgentDataField(description = "存储路径")
    @TableField(value = "storage_path")
    private String storagePath;

    @AgentDataField(description = "校验和")
    private String checksum;

    @AgentDataField(description = "是否为目录")
    @TableField(value = "is_directory")
    private boolean isDirectory;

    @AgentDataField(description = "元数据")
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String metadata;

    @AgentDataField(description = "文件版本")
    private Integer version;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
