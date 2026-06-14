package com.agenthub.infrastructure.tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent HTTP工具DTO，仅暴露Agent决策所需的工具信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentHttpToolDTO {
    private String id;
    private String name;
    private String description;
    private String endpoint;
    private String httpMethod;
    private boolean enabled;
}
