package com.agenthub.domain.model.workflow;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 工作流聚合根，管理Agent的工作流图定义。
 */
public class DagWorkflow {
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

    public DagWorkflow() {
    }

    private DagWorkflow(String id, String tenantId, String workspaceId, String workflowCode,
                     String name, String description, String graphDefinition, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.workflowCode = workflowCode;
        this.name = name;
        this.description = description;
        this.graphDefinition = graphDefinition;
        this.status = "DRAFT";
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static DagWorkflow create(String tenantId, String workspaceId, String workflowCode,
                                  String name, String description, String graphDefinition) {
        return new DagWorkflow(randomId(), tenantId, workspaceId, workflowCode,
                name, description, graphDefinition, Instant.now());
    }

    public void update(String name, String description, String graphDefinition) {
        this.name = name;
        this.description = description;
        this.graphDefinition = graphDefinition;
        this.updatedAt = Instant.now();
    }

    public void publish() {
        this.status = "PUBLISHED";
        this.updatedAt = Instant.now();
    }

    public void unpublish() {
        this.status = "DRAFT";
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getWorkflowCode() {
        return workflowCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getGraphDefinition() {
        return graphDefinition;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGraphDefinition(String graphDefinition) {
        this.graphDefinition = graphDefinition;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
