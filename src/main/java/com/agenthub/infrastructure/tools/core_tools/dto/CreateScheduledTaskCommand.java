package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 创建定时任务命令。
 */
@Data
@AllArgsConstructor
public class CreateScheduledTaskCommand {
    private String cronExpression;
    private String taskName;
    private String taskType;
    private String prompt;
    private String agentId;
}
