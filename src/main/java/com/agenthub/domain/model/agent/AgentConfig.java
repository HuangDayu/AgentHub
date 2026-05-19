package com.agenthub.domain.model.agent;

import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Agent配置关联 - 纵向表，管理Agent与各种配置的关联关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfig {
    private String id;
    private String agentId;
    private AgentConfigCategory category;
    private AgentConfigType type;
    private String configId;
    private String name;
    private String description;
    private int priority;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public AgentConfig(String agentId, AgentConfigCategory category, AgentConfigType type, String configId, String name, String description, int priority, boolean enabled) {
        this.agentId = agentId;
        this.category = category;
        this.type = type;
        this.configId = configId;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.enabled = enabled;
    }
}
