package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * 创建子Agent工具输入。
 */
@Data
public class CreateSubagentToolInput {
    @ToolParam(description = "子Agent名称")
    private String name;
    @ToolParam(description = "系统提示词")
    private String systemPrompt;
    @ToolParam(description = "要执行的任务")
    private String task;
    @ToolParam(required = false, description = "工具列表")
    private List<String> tools;
    @ToolParam(required = false, description = "知识库ID列表")
    private String knowledgeIds;
    @ToolParam(required = false, description = "模型配置ID")
    private String modelConfigId;
}
