package com.agenthub.domain.model;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 智能体聚合根，管理配置关联。
 */
public class Agent {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String agentCode;
    private String name;
    private String description;
    private String status;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public Agent() {
        this.id = randomId();
        this.status = "DRAFT";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Agent create(String tenantId, String workspaceId, String agentCode,
                               String name, String description) {
        Agent agent = new Agent();
        agent.tenantId = tenantId;
        agent.workspaceId = workspaceId;
        agent.agentCode = agentCode;
        agent.name = name;
        agent.description = description;
        return agent;
    }

    public Agent enabled() {
        this.status = "PUBLISHED";
        this.enabled = true;
        this.updatedAt = Instant.now();
        return this;
    }

    public Agent unenabled() {
        this.status = "DRAFT";
        this.enabled = false;
        this.updatedAt = Instant.now();
        return this;
    }

    public Agent update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
        return this;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    // Setters for reconstruction
    public void setId(String id) {
        this.id = id;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
