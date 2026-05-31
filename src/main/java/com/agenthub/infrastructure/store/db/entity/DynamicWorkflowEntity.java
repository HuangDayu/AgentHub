package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 动态工作流持久化对象。
 */
@Data
@TableName("dynamic_workflow")
public class DynamicWorkflowEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "agent_id")
    private String agentId;
    @TableField(value = "session_id")
    private String sessionId;
    private String task;
    private String pattern;
    private String status;
    private String result;
    @TableField(value = "max_concurrent_agents")
    private int maxConcurrentAgents;
    @TableField(value = "total_tokens_used")
    private int totalTokensUsed;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
