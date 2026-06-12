package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStepToolInput {
    @ToolParam(description = "计划ID")
    private String planId;
    @ToolParam(description = "步骤ID")
    private String stepId;
    @ToolParam(description = "新状态")
    private String status;
    @ToolParam(required = false, description = "执行结果或错误信息")
    private String output;
}
