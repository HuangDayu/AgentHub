package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 执行计划持久化对象。
 */
@Data
@TableName("agent_execution_plan")
public class ExecutionPlanEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "agent_id")
    private String agentId;
    @TableField(value = "session_id")
    private String sessionId;
    private String goal;
    private String status;
    @TableField(value = "current_step_index")
    private int currentStepIndex;
    private String result;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
