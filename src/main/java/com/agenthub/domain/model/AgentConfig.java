package com.agenthub.domain.model;

import lombok.Getter;

import java.time.Instant;

import static com.agenthub.domain.model.AgentConfigType.*;

/**
 * Agent配置关联 - 纵向表，管理Agent与各种配置的关联关系
 */
public record AgentConfig(
        String id,
        String agentId,
        AgentConfigCategory category,
        AgentConfigType type,
        String configId,
        String name,
        String description,
        int priority,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {

    public static AgentConfig create(String agentId, AgentConfigCategory category, AgentConfigType type,
                                     String configId, String name, String description,
                                     int priority, boolean enabled) {
        Instant now = Instant.now();
        return new AgentConfig(null, agentId, category, type, configId, name, description, priority, enabled, now, now);
    }

    public AgentConfig update(String configId, String name, String description, Integer priority, Boolean enabled) {
        return new AgentConfig(
                this.id, this.agentId, this.category, this.type,
                configId != null ? configId : this.configId,
                name != null ? name : this.name,
                description != null ? description : this.description,
                priority != null ? priority : this.priority,
                enabled != null ? enabled : this.enabled,
                this.createdAt, Instant.now()
        );
    }
}
