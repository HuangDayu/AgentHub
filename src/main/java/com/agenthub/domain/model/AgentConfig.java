package com.agenthub.domain.model;

import lombok.Getter;

import java.time.Instant;

import static com.agenthub.domain.model.AgentConfig.Type.*;

/**
 * Agent配置关联 - 纵向表，管理Agent与各种配置的关联关系
 */
public record AgentConfig(
        String id,
        String agentId,
        Category category,
        Type type,
        String configId,
        String name,
        String description,
        int priority,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    @Getter
    public enum Category {
        STRATEGY("策略", "Agent的策略相关配置", RETRIEVAL_STRATEGY, TOOL_STRATEGY, MODEL_STRATEGY, GUARDRAIL_STRATEGY),   // 策略
        TOOL("工具", "Agent的工具相关配置", MCP_TOOL, SKILL_TOOL, SYSTEM_TOOL, HTTP_TOOL),       // 工具
        PROMPT("提示词", "Agent的提示词相关配置", SYSTEM_PROMPT, ASSISTANT_PROMPT),     // 提示词
        MODEL("模型", "Agent的模型相关配置", CHAT_MODEL, EMBEDDING_MODEL),       // 模型
        KNOWLEDGE("知识库", "Agent的知识库相关配置", KNOWLEDGE_BASE);   // 知识库

        private final String displayName;
        private final String description;
        private final Type[] types;

        Category(String displayName, String description, Type... types) {
            this.displayName = displayName;
            this.description = description;
            this.types = types;
        }

    }

    @Getter
    public enum Type {
        RETRIEVAL_STRATEGY("检索策略", "知识检索策略配置"),      // 检索策略
        TOOL_STRATEGY("工具策略", "工具调用策略配置"),  // 工具策略
        MODEL_STRATEGY("模型策略", "模型调用策略配置"), // 模型策略
        GUARDRAIL_STRATEGY("护栏策略", "输入输出护栏策略"),      // 护栏策略
        SYSTEM_PROMPT("系统提示词", "Agent系统提示词模板"),  // 系统提示词
        ASSISTANT_PROMPT("助理提示词", "Agent助理提示词模板"), // 助手提示词
        CHAT_MODEL("聊天模型", "对话模型配置"), // 聊天模型
        EMBEDDING_MODEL("嵌入模型", "向量嵌入模型配置"), // 嵌入模型
        KNOWLEDGE_BASE("知识库", "知识库配置"),   // 知识库
        MCP_TOOL("MCP", "MCP协议工具配置"),        // MCP工具
        SKILL_TOOL("Skill", "Skill技能配置"),        // Skill工具
        SYSTEM_TOOL("System", "System工具配置"),        // Function工具
        HTTP_TOOL("Http", "Http工具配置");       // HTTP工具

        private final String displayName;
        private final String description;

        Type(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

    }

    public static AgentConfig create(String agentId, Category category, Type type,
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
