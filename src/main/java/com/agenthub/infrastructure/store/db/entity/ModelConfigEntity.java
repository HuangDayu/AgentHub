package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.enums.ModelType;
import com.agenthub.infrastructure.store.db.mapper.ModelConfigMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.MODEL;
import static com.agenthub.domain.enums.AgentConfigType.ALL_TYPE;

/**
 * 模型配置持久化对象（PO），映射 model_config 表。
 */
@Data
@TableName("model_config")
@ConfigChangeListenerEntity(category = MODEL, type = ALL_TYPE)
@AgentDataModel(
    name = "模型配置",
    description = "AI模型配置，包括供应商、API密钥和模型参数",
    domain = "模型管理",
    mapper = ModelConfigMybatisMapper.class
)
public class ModelConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @AgentDataField(description = "配置名称", required = true, filterable = true)
    @TableField("name")
    private String name;

    @AgentDataField(description = "模型类型", filterable = true, enumType = ModelType.class)
    @TableField("type")
    private String type;

    @AgentDataField(description = "模型供应商", enumType = ModelSupplier.class)
    @TableField("supplier")
    private String supplier;

    @AgentDataField(hidden = true, sensitive = true)
    @TableField("api_key")
    private String apiKey;

    @AgentDataField(description = "API基础URL")
    @TableField("base_url")
    private String baseUrl;

    @AgentDataField(description = "模型名称")
    @TableField("model")
    private String model;

    @AgentDataField(description = "是否启用")
    @TableField("enabled")
    private Boolean enabled;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

    @TableField("created_by")
    private String createdBy;

    public ModelConfigEntity() {
    }


}