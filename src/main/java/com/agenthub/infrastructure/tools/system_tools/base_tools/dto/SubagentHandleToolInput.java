package com.agenthub.infrastructure.tools.system_tools.base_tools.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 子Agent句柄工具输入。
 */
@Data
public class SubagentHandleToolInput {
    @ToolParam(description = "子Agent ID")
    private String subagentId;
    @ToolParam(description = "子Agent会话ID")
    private String subsessionId;
}
