package com.agenthub.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

/**
 * 模型配置持久化对象（PO），映射 model_config 表。
 */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
}