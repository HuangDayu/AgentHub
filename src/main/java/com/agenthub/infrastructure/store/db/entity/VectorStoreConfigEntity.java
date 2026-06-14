package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.VectorStoreType;
import com.agenthub.infrastructure.store.db.mapper.VectorStoreConfigMybatisMapper;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * 向量库配置数据库持久化对象。
 * <p>
 * 映射到 vector_store_config 表。
 * </p>
 */
@Data
@TableName("vector_store_config")
@AgentDataModel(
    name = "向量库配置",
    description = "向量存储配置，管理向量数据库的连接和参数设置",
    domain = "数据管理",
    mapper = VectorStoreConfigMybatisMapper.class
)
public class VectorStoreConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @AgentDataField(description = "配置名称", filterable = true)
    private String name;
    @AgentDataField(description = "向量库类型", filterable = true, enumType = VectorStoreType.class)
    private String type;
    @AgentDataField(description = "主机地址")
    private String host;
    @AgentDataField(description = "端口号")
    private Integer port;
    @AgentDataField(description = "API密钥", sensitive = true)
    private String apiKey;
    @AgentDataField(description = "集合名称")
    private String collectionName;
    @AgentDataField(description = "扩展参数JSON")
    private String extraParams;
    @AgentDataField(description = "是否启用")
    private Boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
