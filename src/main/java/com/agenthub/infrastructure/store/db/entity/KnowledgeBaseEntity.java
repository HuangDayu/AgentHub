package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.KnowledgeBaseStatus;
import com.agenthub.infrastructure.store.db.mapper.KnowledgeBaseMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.KNOWLEDGE;
import static com.agenthub.domain.enums.AgentConfigType.KNOWLEDGE_BASE;

/**
 * 知识库数据库实体。
 * <p>
 * 映射到 knowledge_base 表，存储知识库的元数据和索引版本信息。
 * </p>
 */
@Data
@TableName(value = "knowledge_base", autoResultMap = true)
@ConfigChangeListenerEntity(category = KNOWLEDGE, type = KNOWLEDGE_BASE)
@AgentDataModel(
    name = "知识库",
    description = "用于存储和检索文档的知识库，支持版本管理和更新操作",
    domain = "知识管理",
    mapper = KnowledgeBaseMybatisMapper.class
)
public class KnowledgeBaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    
    @AgentDataField(description = "知识库唯一标识")
    @TableField("kb_code")
    private String kbCode;
    
    @AgentDataField(description = "知识库名称", required = true, filterable = true)
    private String name;
    
    @AgentDataField(description = "知识库描述")
    private String description;
    
    @AgentDataField(description = "知识库状态", filterable = true, enumType = KnowledgeBaseStatus.class)
    private String status;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    private String createdBy;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
    private String updatedBy;
    /**
     * 租户向量数据库配置ID（可选，外键引用 vector_store_config）
     */
    @AgentDataField(description = "向量数据库配置ID")
    private String vectorStoreConfigId;
    /**
     * 租户嵌入模型配置ID（可选，外键引用 model_config）
     */
    @AgentDataField(description = "嵌入模型配置ID")
    private String embeddingModelConfigId;
    /**
     * 分词模型配置 ID（可选，关联模型配置用于分词）
     */
    @AgentDataField(description = "对话模型配置ID")
    private String chatModelConfigId;

}

