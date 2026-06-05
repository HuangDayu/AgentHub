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

    /**
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String tenantId;
        private final String workspaceId;
        private final String agentId;
        private final String name;
        private final String memoryType;
        private final String content;
        private final String metadata;
        private final double importance;
        private final Instant expiresAt;

        public CreationSpec(String tenantId, String workspaceId, String agentId, String name,
                               String memoryType, String content, String metadata,
                               double importance, Instant expiresAt) {
            this.tenantId = tenantId;
            this.workspaceId = workspaceId;
            this.agentId = agentId;
            this.name = name;
            this.memoryType = memoryType;
            this.content = content;
            this.metadata = metadata;
            this.importance = importance;
            this.expiresAt = expiresAt;
        }
    }

    /**
     * 创建新的记忆实例。
     */
    public static Memory create(CreationSpec spec) {
        Memory memory = new Memory();
        Instant now = Instant.now();
        copySpecFields(memory, spec);
        memory.id = randomId();
        memory.memoryType = MemoryType.valueOf(spec.memoryType);
        memory.createdAt = now;
        memory.updatedAt = now;
        return memory;
    }

    private static void copySpecFields(Memory memory, CreationSpec spec) {
        memory.tenantId = spec.tenantId;
        memory.workspaceId = spec.workspaceId;
        memory.agentId = spec.agentId;
        memory.name = spec.name;
        memory.content = spec.content;
        memory.metadata = spec.metadata;
        memory.importance = spec.importance;
        memory.expiresAt = spec.expiresAt;
    }

    /**
     * 更新记忆内容。
     */
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
