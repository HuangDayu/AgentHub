package com.agenthub.infrastructure.store.db.entity;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.infrastructure.store.db.mapper.ToolStrategyMybatisMapper;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.STRATEGY;
import static com.agenthub.domain.enums.AgentConfigType.TOOL_STRATEGY;

@Data
@TableName("tool_policy")
@ConfigChangeListenerEntity(category = STRATEGY, type = TOOL_STRATEGY)
@AgentDataModel(
    name = "工具策略",
    description = "工具策略配置，管理工具调用限制",
    domain = "策略管理",
    mapper = ToolStrategyMybatisMapper.class
)
public class ToolStrategyEntity {
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

    @AgentDataField(description = "最大并发调用数")
    private Integer maxConcurrentCalls;

    @AgentDataField(description = "超时时间（秒）")
    private Integer timeoutSeconds;

    @AgentDataField(description = "重试次数")
    private Integer retryCount;

    @AgentDataField(description = "是否启用降级")
    private Boolean fallbackEnabled;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
