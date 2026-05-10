package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowOutput {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String workflowCode;
    private String name;
    private String description;
    private String graphDefinition;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
