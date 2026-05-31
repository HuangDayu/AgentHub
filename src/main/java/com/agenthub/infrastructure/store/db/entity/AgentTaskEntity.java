package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

/**
 * 工作流任务持久化对象。
 */
@Data
@TableName("agent_task")
public class AgentTaskEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "stage_id")
    private String stageId;
    @TableField(value = "workflow_id")
    private String workflowId;
    @TableField(value = "task_description")
    private String taskDescription;
    @TableField(value = "subagent_id")
    private String subagentId;
    @TableField(value = "subsession_id")
    private String subsessionId;
    private String status;
    private String result;
    @TableField(value = "model_config_id")
    private String modelConfigId;
    @TableField(value = "tool_names")
    private String toolNames;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
