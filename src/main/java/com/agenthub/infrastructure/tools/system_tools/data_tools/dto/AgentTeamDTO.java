package com.agenthub.infrastructure.tools.system_tools.data_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent团队DTO，仅暴露Agent决策所需的团队信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentTeamDTO {
    private String id;
    private String name;
    private String description;
    private String coordinationMode;
    private String status;
}
