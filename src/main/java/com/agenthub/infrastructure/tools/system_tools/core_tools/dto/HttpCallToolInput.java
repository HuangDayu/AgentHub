package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpCallToolInput {
    @ToolParam(description = "请求方法：GET/POST/PUT/DELETE")
    private String method;
    @ToolParam(description = "完整的请求URL")
    private String url;
    @ToolParam(description = "请求头（JSON格式，可选）")
    private String headersJson;
    @ToolParam(description = "请求体（JSON格式，POST/PUT时使用）")
    private String body;
}
