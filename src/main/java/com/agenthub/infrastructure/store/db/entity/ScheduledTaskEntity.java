package com.agenthub.infrastructure.store.db.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

@Data
@TableName("scheduled_task")
public class ScheduledTaskEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    
    private String taskCode;
    private String name;
    private String description;
    private String taskType;
    private String cronExpression;
    private String executorConfig;
    private String prompt;
    private boolean enabled;
    private Instant lastExecuteTime;
    private Instant nextExecuteTime;
    private String status;
    private String agentId;
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String lastRunResult;
    private int runCount;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
    
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(value = "updated_by", fill = FieldFill.INSERT)
    private String updatedBy;
}
