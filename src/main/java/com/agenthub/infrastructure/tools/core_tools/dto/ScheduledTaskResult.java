package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定时任务结果DTO。
 */
@Data
@NoArgsConstructor
public class ScheduledTaskResult {
    private String id;
    private String name;
    private String taskType;
    private String cronExpression;
    private String prompt;
    private boolean enabled;
    private String status;
    private boolean scheduled;
    private String agentId;
    private String lastRunResult;
    private int runCount;
}
