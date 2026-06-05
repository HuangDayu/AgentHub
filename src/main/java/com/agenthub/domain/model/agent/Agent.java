package com.agenthub.domain.model.agent;

import lombok.Data;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 智能体聚合根，管理配置关联。
 */
@Data
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

    /**
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String tenantId;
        private final String workspaceId;
        private final String agentCode;
        private final String name;
        private final String description;

        public CreationSpec(String tenantId, String workspaceId, String agentCode,
                               String name, String description) {
            this.tenantId = tenantId;
            this.workspaceId = workspaceId;
            this.agentCode = agentCode;
            this.name = name;
            this.description = description;
        }
    }

    /**
     * 创建新的智能体实例。
     */
    public static Agent create(CreationSpec spec) {
        Agent agent = new Agent();
        agent.tenantId = spec.tenantId;
        agent.workspaceId = spec.workspaceId;
        agent.agentCode = spec.agentCode;
        agent.name = spec.name;
        agent.description = spec.description;
        return agent;
    }

    /**
     * 启用智能体。
     */
    public Agent enabled() {
        this.status = "PUBLISHED";
        this.enabled = true;
        this.updatedAt = Instant.now();
        return this;
    }

    /**
     * 禁用智能体。
     */
    public Agent unenabled() {
        this.status = "DRAFT";
        this.enabled = false;
        this.updatedAt = Instant.now();
        return this;
    }

    /**
     * 更新智能体基础信息。
     */
    public Agent update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
        return this;
    }


}
