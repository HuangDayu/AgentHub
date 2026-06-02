package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * 技能文件实体。
 */
@Data
@TableName("skill_file")
public class SkillFileEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "skill_id")
    private String skillId;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @TableField(value = "file_path")
    private String filePath;

    @TableField(value = "file_name")
    private String fileName;

    @TableField(value = "file_ext")
    private String fileExt;

    @TableField(value = "file_size")
    private Long fileSize;

    @TableField(value = "file_type")
    private String fileType;

    private String encoding;

    @TableField(value = "storage_path")
    private String storagePath;

    private String checksum;

    @TableField(value = "is_directory")
    private boolean isDirectory;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String metadata;

    private Integer version;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
