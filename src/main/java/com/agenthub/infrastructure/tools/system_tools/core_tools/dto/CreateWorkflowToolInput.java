package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowToolInput {
    @ToolParam(description = "任务描述")
    private String task;
    @ToolParam(description = "编排模式: FAN_OUT/PIPELINE/JUDGE")
    private String pattern;
    @ToolParam(description = "子任务描述（逗号分隔）")
    private String subtasks;
}
