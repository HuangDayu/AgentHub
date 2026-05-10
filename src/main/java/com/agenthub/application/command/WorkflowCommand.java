package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCommand {
    private String tenantId;
    private String workspaceId;
    private String workflowCode;
    private String name;
    private String description;
    private String graphDefinition;
}
