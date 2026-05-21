package com.agenthub.infrastructure.tools.system_tools.data_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent定时任务DTO，仅暴露Agent决策所需的定时任务信息，不含执行配置和提示词。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentScheduledTaskDTO {
    private String id;
    private String name;
    private String description;
    private String taskType;
    private String cronExpression;
    private boolean enabled;
    private String status;
    private LocalDateTime lastExecuteTime;
    private LocalDateTime nextExecuteTime;
}
