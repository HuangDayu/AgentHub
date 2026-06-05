package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 计划步骤持久化对象。
 */
@Data
@TableName("agent_plan_step")
public class PlanStepEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "plan_id")
    private String planId;
    @TableField(value = "step_order")
    private int stepOrder;
    private String description;
    @TableField(value = "tool_name")
    private String toolName;
    @TableField(value = "tool_input")
    private String toolInput;
    private String status;
    @TableField(value = "step_output")
    private String stepOutput;
    @TableField(value = "subagent_id")
    private String subagentId;
    @TableField(value = "subsession_id")
    private String subsessionId;
    @TableField(value = "depends_on")
    private String dependsOn;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
