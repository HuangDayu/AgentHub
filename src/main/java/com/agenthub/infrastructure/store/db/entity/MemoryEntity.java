package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.MemoryType;
import com.agenthub.infrastructure.store.db.mapper.MemoryMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("memory")
@AgentDataModel(
    name = "记忆",
    description = "Agent的记忆信息，用于存储对话历史、知识片段和上下文记忆",
    domain = "记忆管理",
    mapper = MemoryMybatisMapper.class
)
public class MemoryEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @AgentDataField(description = "关联Agent的ID")
    private String agentId;
    @AgentDataField(description = "记忆名称")
    private String name;
    @AgentDataField(description = "记忆类型", filterable = true, enumType = MemoryType.class)
    @TableField(value = "memory_type")
    private String memoryType;
    @AgentDataField(description = "记忆内容")
    private String content;
    @AgentDataField(description = "元数据")
    private String metadata;
    @AgentDataField(description = "重要性评分")
    private double importance;
    @AgentDataField(description = "过期时间")
    @TableField(value = "expires_at")
    private Instant expiresAt;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
