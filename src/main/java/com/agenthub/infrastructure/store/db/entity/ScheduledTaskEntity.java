package com.agenthub.infrastructure.store.db.entity;

import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.ScheduledTaskStatus;
import com.agenthub.domain.enums.ScheduledTaskType;
import com.agenthub.infrastructure.store.db.mapper.ScheduledTaskMyBatisMapper;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

@Data
@TableName("scheduled_task")
@AgentDataModel(
    name = "定时任务",
    description = "定时任务配置，支持cron表达式调度和多种执行器类型",
    domain = "任务管理",
    mapper = ScheduledTaskMyBatisMapper.class
)
public class ScheduledTaskEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    
    @AgentDataField(description = "任务编码")
    private String taskCode;
    @AgentDataField(description = "任务名称")
    private String name;
    @AgentDataField(description = "任务描述")
    private String description;
    @AgentDataField(description = "任务类型", filterable = true, enumType = ScheduledTaskType.class)
    private String taskType;
    @AgentDataField(description = "cron表达式")
    private String cronExpression;
    @AgentDataField(description = "执行器配置")
    private String executorConfig;
    @AgentDataField(description = "执行提示词")
    private String prompt;
    @AgentDataField(description = "是否启用")
    private boolean enabled;
    @AgentDataField(description = "最后执行时间")
    private Instant lastExecuteTime;
    @AgentDataField(description = "下次执行时间")
    private Instant nextExecuteTime;
    @AgentDataField(description = "任务状态", filterable = true, enumType = ScheduledTaskStatus.class)
    private String status;
    @AgentDataField(description = "关联Agent ID")
    private String agentId;
    @AgentDataField(description = "最后运行结果")
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String lastRunResult;
    @AgentDataField(description = "运行次数")
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
