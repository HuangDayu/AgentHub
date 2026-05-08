package com.agenthub.infrastructure.store.db.entity;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

import static com.agenthub.domain.model.AgentConfigCategory.STRATEGY;
import static com.agenthub.domain.model.AgentConfigType.GUARDRAIL_STRATEGY;
import static com.agenthub.domain.model.AgentConfigType.RETRIEVAL_STRATEGY;

@Data
@TableName("app.retrieval_policy")
@ConfigChangeListenerEntity(category = STRATEGY, type = RETRIEVAL_STRATEGY)
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

}
