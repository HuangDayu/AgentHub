package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStepToolInput {
    @ToolParam(description = "计划ID")
    private String planId;
    @ToolParam(description = "步骤描述")
    private String description;
    @ToolParam(required = false, description = "使用的工具名称")
    private String toolName;
    @ToolParam(required = false, description = "工具调用参数（JSON）")
    private String toolInput;
}
