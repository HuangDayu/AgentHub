package com.agenthub.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * 知识库数据库实体。
 * <p>
 * 映射到 app.knowledge_base 表，存储知识库的元数据和索引版本信息。
 * </p>
 */
@TableName(value = "app.knowledge_base", autoResultMap = true)
public class KnowledgeBaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id",fill = FieldFill.INSERT)
    private String workspaceId;
    @TableField("kb_code")
    private String kbCode;
    private String name;
    private String description;
    private String status;
    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private Instant createdAt;
    private String createdBy;
    @TableField(value = "updated_at",fill = FieldFill.UPDATE)
    private Instant updatedAt;
    private String updatedBy;
    /** 租户向量数据库配置ID（可选，外键引用 vector_store_config） */
    private String vectorStoreConfigId;
    /** 租户嵌入模型配置ID（可选，外键引用 model_config） */
    private String embeddingModelConfigId;
    /** 分词模型配置 ID（可选，关联模型配置用于分词） */
    private String chatModelConfigId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getKbCode() {
        return kbCode;
    }

    public void setKbCode(String kbCode) {
        this.kbCode = kbCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getVectorStoreConfigId() {
        return vectorStoreConfigId;
    }

    public void setVectorStoreConfigId(String vectorStoreConfigId) {
        this.vectorStoreConfigId = vectorStoreConfigId;
    }

    public String getEmbeddingModelConfigId() {
        return embeddingModelConfigId;
    }

    public void setEmbeddingModelConfigId(String embeddingModelConfigId) {
        this.embeddingModelConfigId = embeddingModelConfigId;
    }

    public String getChatModelConfigId() {
        return chatModelConfigId;
    }

    public void setChatModelConfigId(String chatModelConfigId) {
        this.chatModelConfigId = chatModelConfigId;
    }
}

