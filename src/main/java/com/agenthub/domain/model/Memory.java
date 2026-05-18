package com.agenthub.domain.model;

import com.agenthub.domain.enums.MemoryType;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 记忆聚合根，管理Agent的长期记忆存储。
 */
public class Memory {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String agentId;
    private String name;
    private MemoryType memoryType;
    private String content;
    private String metadata;
    private double importance;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Memory() {
    }

    private Memory(String id, String tenantId, String workspaceId, String agentId, String name,
                   String memoryType, String content, String metadata,
                   double importance, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.agentId = agentId;
        this.name = name;
        this.memoryType = MemoryType.valueOf(memoryType);
        this.content = content;
        this.metadata = metadata;
        this.importance = importance;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static Memory create(String tenantId, String workspaceId, String agentId, String name,
                                String memoryType, String content, String metadata,
                                double importance, Instant expiresAt) {
        return new Memory(randomId(), tenantId, workspaceId, agentId, name,
                memoryType, content, metadata, importance, expiresAt, Instant.now());
    }

    public void update(String content, String metadata, double importance, Instant expiresAt) {
        this.content = content;
        this.metadata = metadata;
        this.importance = importance;
        this.expiresAt = expiresAt;
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

    public String getAgentId() {
        return agentId;
    }

    public String getName() {
        return name;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public String getContent() {
        return content;
    }

    public String getMetadata() {
        return metadata;
    }

    public double getImportance() {
        return importance;
    }

    public Instant getExpiresAt() {
        return expiresAt;
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

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setImportance(double importance) {
        this.importance = importance;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
