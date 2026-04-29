package com.agenthub.domain.model;

import java.time.Instant;

/**
 * Agent配置关联 - 纵向表，管理Agent与各种配置的关联关系
 */
public record AgentConfig(
        String id,
        String agentId,
        Category category,
        Type type,
        String configId,
        String description,
        int priority,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public enum Category {
        STRATEGY,   // 策略
        TOOL,       // 工具
        PROMPT,     // 提示词
        MODEL,       // 模型
        KNOWLEDGE,   // 知识库
    }

    public enum Type {
        RETRIEVAL_STRATEGY,      // 检索策略
        TOOL_STRATEGY,  // 工具策略
        MODEL_STRATEGY, // 模型策略
        GUARDRAIL_STRATEGY,      // 护栏策略
        SYSTEM_PROMPT,  // 系统提示词
        MCP_TOOL,        // MCP工具
        CHAT_MODEL, // 聊天模型
        EMBEDDING_MODEL, // 嵌入模型
        KNOWLEDGE_BASE,   // 知识库
    }

    public static AgentConfig create(String agentId, Category category, Type type,
                                     String configId, String description, int priority) {
        Instant now = Instant.now();
        return new AgentConfig(null, agentId, category, type, configId, description, priority, true, now, now);
    }

    public AgentConfig update(String configId, String description, Integer priority, Boolean enabled) {
        return new AgentConfig(
                this.id, this.agentId, this.category, this.type,
                configId != null ? configId : this.configId,
                description != null ? description : this.description,
                priority != null ? priority : this.priority,
                enabled != null ? enabled : this.enabled,
                this.createdAt, Instant.now()
        );
    }
}
