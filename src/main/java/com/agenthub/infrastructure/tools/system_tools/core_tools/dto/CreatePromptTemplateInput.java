package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePromptTemplateInput {
    @ToolParam(description = "模板名称")
    private String name;
    @ToolParam(description = "模板描述")
    private String description;
    @ToolParam(description = "模板类别(SYSTEM/USER/ASSISTANT/GENERAL)")
    private String category;
    @ToolParam(description = "模板内容")
    private String content;
    @ToolParam(required = false, description = "是否启用")
    private Boolean active;
}
