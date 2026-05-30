package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent工作流DTO，仅暴露Agent决策所需的工作流信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentWorkflowDTO {
    private String id;
    private String workflowCode;
    private String name;
    private String description;
    private String status;
}
