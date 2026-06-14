package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.GuardrailStrategyMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.STRATEGY;
import static com.agenthub.domain.enums.AgentConfigType.GUARDRAIL_STRATEGY;

@Data
@TableName("guardrail_policy")
@ConfigChangeListenerEntity(category = STRATEGY, type = GUARDRAIL_STRATEGY)
@AgentDataModel(
    name = "护栏策略",
    description = "护栏策略配置，管理输入输出校验与安全检测",
    domain = "策略管理",
    mapper = GuardrailStrategyMybatisMapper.class
)
public class GuardrailStrategyEntity {
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

    @AgentDataField(description = "是否启用输入校验")
    private Boolean inputValidationEnabled;

    @AgentDataField(description = "是否启用输出校验")
    private Boolean outputValidationEnabled;

    @AgentDataField(description = "是否启用PII检测")
    private Boolean piiDetectionEnabled;

    @AgentDataField(description = "是否启用PII脱敏")
    private Boolean piiMaskingEnabled;

    @AgentDataField(description = "是否启用提示词注入检测")
    private Boolean promptInjectionDetection;

    @AgentDataField(description = "最大输入长度")
    private Integer maxInputLength;

    @AgentDataField(description = "最大输出长度")
    private Integer maxOutputLength;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
