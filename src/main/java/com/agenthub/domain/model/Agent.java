package com.agenthub.domain.model;

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


}
