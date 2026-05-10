package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowRequest {
    private String tenantId;
    private String workspaceId;
    private String workflowCode;
    private String name;
    private String description;
    private String graphDefinition;
}
