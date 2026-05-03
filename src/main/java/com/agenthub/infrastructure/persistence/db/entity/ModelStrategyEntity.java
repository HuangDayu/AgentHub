package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("app.model_policy")
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
