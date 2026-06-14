package com.agenthub.infrastructure.store.db.entity;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.ModelStrategyMybatisMapper;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.STRATEGY;
import static com.agenthub.domain.enums.AgentConfigType.MODEL_STRATEGY;

@Data
@TableName("model_policy")
@ConfigChangeListenerEntity(category = STRATEGY, type = MODEL_STRATEGY)
@AgentDataModel(
    name = "模型策略",
    description = "模型策略配置，管理模型调用参数",
    domain = "策略管理",
    mapper = ModelStrategyMybatisMapper.class
)
public class ModelStrategyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @AgentDataField(description = "策略名称", filterable = true)
    private String name;

    @AgentDataField(description = "策略描述")
    private String description;

    @AgentDataField(description = "温度参数")
    private Double temperature;

    @AgentDataField(description = "最大Token数")
    private Integer maxTokens;

    @AgentDataField(description = "Top-P采样参数")
    private Double topP;

    @AgentDataField(description = "频率惩罚")
    private Double frequencyPenalty;

    @AgentDataField(description = "存在惩罚")
    private Double presencePenalty;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
