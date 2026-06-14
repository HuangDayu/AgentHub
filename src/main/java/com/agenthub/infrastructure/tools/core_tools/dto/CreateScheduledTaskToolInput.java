package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduledTaskToolInput {
    @ToolParam(description = "Cron表达式，如 '0 9 * * ?' 表示每天9点")
    private String cronExpression;
    @ToolParam(description = "任务名称")
    private String taskName;
    @ToolParam(description = "任务类型：AGENT_CHAT / WORKFLOW / SYSTEM")
    private String taskType;
    @ToolParam(description = "执行提示词或命令内容")
    private String prompt;
    @ToolParam(required = false, description = "执行该任务的AgentID（可选，为空则使用工作空间内第一个Agent）")
    private String agentId;
}
