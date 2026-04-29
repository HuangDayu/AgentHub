package com.agenthub.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@TableName("app.retrieval_policy")
public class RetrievalStrategyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id",fill = FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String description;
    private String retrievalType;
    private Integer topK;
    private Double scoreThreshold;
    private Boolean enableRerank;
    private Boolean enableQueryRewrite;
    private Boolean enableTextSearch;
    private Boolean enableVectorSearch;
    private String rerankModel;
    private Double vectorWeight;
    private Double keywordWeight;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRetrievalType() { return retrievalType; }
    public void setRetrievalType(String retrievalType) { this.retrievalType = retrievalType; }
    public Integer getTopK() { return topK; }
    public void setTopK(Integer topK) { this.topK = topK; }
    public Double getScoreThreshold() { return scoreThreshold; }
    public void setScoreThreshold(Double scoreThreshold) { this.scoreThreshold = scoreThreshold; }
    public Boolean getEnableRerank() { return enableRerank; }
    public void setEnableRerank(Boolean enableRerank) { this.enableRerank = enableRerank; }
    public String getRerankModel() { return rerankModel; }
    public void setRerankModel(String rerankModel) { this.rerankModel = rerankModel; }
    public Double getVectorWeight() { return vectorWeight; }
    public void setVectorWeight(Double vectorWeight) { this.vectorWeight = vectorWeight; }
    public Double getKeywordWeight() { return keywordWeight; }
    public void setKeywordWeight(Double keywordWeight) { this.keywordWeight = keywordWeight; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getEnableQueryRewrite() {
        return enableQueryRewrite;
    }

    public void setEnableQueryRewrite(Boolean enableQueryRewrite) {
        this.enableQueryRewrite = enableQueryRewrite;
    }

    public Boolean getEnableTextSearch() {
        return enableTextSearch;
    }

    public void setEnableTextSearch(Boolean enableTextSearch) {
        this.enableTextSearch = enableTextSearch;
    }

    public Boolean getEnableVectorSearch() {
        return enableVectorSearch;
    }

    public void setEnableVectorSearch(Boolean enableVectorSearch) {
        this.enableVectorSearch = enableVectorSearch;
    }
}
