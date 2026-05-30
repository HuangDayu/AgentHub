package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * 创建执行计划步骤输入。
 */
@Data
public class PlanStepToolInput {
    @ToolParam(description = "步骤描述")
    private String description;
    @ToolParam(required = false, description = "使用的工具名称")
    private String toolName;
    @ToolParam(required = false, description = "工具调用参数（JSON）")
    private String toolInput;
    @ToolParam(required = false, description = "依赖的步骤描述列表")
    private List<String> dependsOn;
}
