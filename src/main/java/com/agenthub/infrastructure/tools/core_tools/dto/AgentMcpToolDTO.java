package com.agenthub.infrastructure.tools.core_tools.dto;

import com.agenthub.domain.model.tools.McpTool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent MCP工具DTO，仅暴露Agent决策所需的工具信息，不含服务器地址、命令等敏感配置。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentMcpToolDTO {
    private String id;
    private String name;
    private String description;
    private McpTool.ServerType serverType;
    private boolean async;
    private boolean enabled;
}
