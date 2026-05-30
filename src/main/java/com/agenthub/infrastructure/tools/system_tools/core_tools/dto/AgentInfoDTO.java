package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent自身信息DTO，仅暴露Agent决策所需的自身信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentInfoDTO {
    private String id;
    private String name;
    private String description;
    private String agentCode;
    private String status;
    private boolean enabled;
}
