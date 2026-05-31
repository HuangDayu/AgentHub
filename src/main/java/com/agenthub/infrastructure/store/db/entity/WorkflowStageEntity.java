package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 工作流阶段持久化对象。
 */
@Data
@TableName("workflow_stage")
public class WorkflowStageEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "workflow_id")
    private String workflowId;
    @TableField(value = "stage_order")
    private int stageOrder;
    private String name;
    @TableField(value = "stage_type")
    private String stageType;
    @TableField(value = "system_prompt")
    private String systemPrompt;
    @TableField(value = "task_template")
    private String taskTemplate;
    @TableField(value = "depends_on")
    private String dependsOn;
    private String status;
    private String output;
    @TableField(value = "completed_task_count")
    private int completedTaskCount;
    @TableField(value = "total_task_count")
    private int totalTaskCount;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
