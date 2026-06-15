package com.agenthub.api.dto;

import com.agenthub.domain.enums.AgentRuntimeCategory;
import com.agenthub.domain.enums.AgentType;
import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String agentCode;
    private String name;
    private String description;
    private String status;
    private boolean enabled;
    private AgentType type;
    private AgentRuntimeCategory runtimeCategory;
    private Instant createdAt;
    private Instant updatedAt;
}
