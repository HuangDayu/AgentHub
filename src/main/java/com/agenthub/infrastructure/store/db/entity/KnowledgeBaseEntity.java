package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

import static com.agenthub.domain.model.AgentConfigCategory.KNOWLEDGE;
import static com.agenthub.domain.model.AgentConfigType.KNOWLEDGE_BASE;

/**
 * 知识库数据库实体。
 * <p>
 * 映射到 knowledge_base 表，存储知识库的元数据和索引版本信息。
 * </p>
 */
@Data
@TableName(value = "knowledge_base", autoResultMap = true)
@ConfigChangeListenerEntity(category = KNOWLEDGE, type = KNOWLEDGE_BASE)
public class KnowledgeBaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @TableField("kb_code")
    private String kbCode;
    private String name;
    private String description;
    private String status;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    private String createdBy;
    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
    private Instant updatedAt;
    private String updatedBy;
    /**
     * 租户向量数据库配置ID（可选，外键引用 vector_store_config）
     */
    private String vectorStoreConfigId;
    /**
     * 租户嵌入模型配置ID（可选，外键引用 model_config）
     */
    private String embeddingModelConfigId;
    /**
     * 分词模型配置 ID（可选，关联模型配置用于分词）
     */
    private String chatModelConfigId;

}

