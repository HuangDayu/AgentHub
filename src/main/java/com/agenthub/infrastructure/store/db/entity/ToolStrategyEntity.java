package com.agenthub.infrastructure.store.db.entity;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.STRATEGY;
import static com.agenthub.domain.enums.AgentConfigType.TOOL_STRATEGY;

@Data
@TableName("tool_policy")
@ConfigChangeListenerEntity(category = STRATEGY, type = TOOL_STRATEGY)
public class ToolStrategyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id",fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id",fill = FieldFill.INSERT)
    private String workspaceId;
    private String name;
    private String description;
    private Integer maxConcurrentCalls;
    private Integer timeoutSeconds;
    private Integer retryCount;
    private Boolean fallbackEnabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;


}
