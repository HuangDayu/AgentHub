package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent系统工具DTO，仅暴露Agent决策所需的系统工具信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentSystemToolDTO {
    private String id;
    private String toolName;
    private String description;
    private String category;
    private int methodCount;
    private boolean enabled;
    private boolean systemTool;
}
