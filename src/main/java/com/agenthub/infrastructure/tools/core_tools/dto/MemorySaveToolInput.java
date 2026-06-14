package com.agenthub.infrastructure.tools.core_tools.dto;

import com.agenthub.domain.enums.MemoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemorySaveToolInput {
    @ToolParam(description = "记忆内容")
    private String content;
    @ToolParam(description = "记忆名称/标题")
    private String name;
    @ToolParam(description = "记忆类型：SHORT_TERM / LONG_TERM / EPISODIC / SEMANTIC")
    private MemoryType memoryType;
}
