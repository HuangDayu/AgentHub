package com.agenthub.application.command;

import com.agenthub.domain.enums.AgentRuntimeCategory;
import com.agenthub.domain.enums.AgentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAgentCommand {
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
}
