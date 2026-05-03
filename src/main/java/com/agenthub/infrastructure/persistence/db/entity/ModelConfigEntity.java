package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * 模型配置持久化对象（PO），映射 model_config 表。
 */
@Data
@TableName("app.model_config")
public class ModelConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("supplier")
    private String supplier;

    @TableField("api_key")
    private String apiKey;

    @TableField("base_url")
    private String baseUrl;

    @TableField("model")
    private String model;

    @TableField("enabled")
    private Boolean enabled;

    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at",fill = FieldFill.UPDATE)
    private Instant updatedAt;

    @TableField("created_by")
    private String createdBy;

    public ModelConfigEntity() {
    }


}