package com.agenthub.infrastructure.store.db.entity;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

import static com.agenthub.domain.model.AgentConfigCategory.STRATEGY;
import static com.agenthub.domain.model.AgentConfigType.GUARDRAIL_STRATEGY;
import static com.agenthub.domain.model.AgentConfigType.MODEL_STRATEGY;

@Data
@TableName("app.model_policy")
@ConfigChangeListenerEntity(category = STRATEGY, type = MODEL_STRATEGY)
public class ModelStrategyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id",fill = FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String description;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private Double frequencyPenalty;
    private Double presencePenalty;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;


}
