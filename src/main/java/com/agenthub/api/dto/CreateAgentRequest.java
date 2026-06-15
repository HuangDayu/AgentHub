package com.agenthub.api.dto;

import com.agenthub.domain.enums.AgentRuntimeCategory;
import com.agenthub.domain.enums.AgentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAgentRequest {
    private String tenantId;
    private String workspaceId;
    private String agentCode;
    private String name;
    private String description;
    private AgentType type;
    private AgentRuntimeCategory runtimeCategory;
}
