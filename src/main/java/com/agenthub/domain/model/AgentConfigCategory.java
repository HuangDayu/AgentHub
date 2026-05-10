package com.agenthub.domain.model;

import lombok.Getter;

import static com.agenthub.domain.model.AgentConfigType.*;

/**
 * @author huangdayu
 */
@Getter
public enum AgentConfigCategory {

    STRATEGY("策略", "Agent的策略相关配置", 1, RETRIEVAL_STRATEGY, TOOL_STRATEGY, MODEL_STRATEGY, GUARDRAIL_STRATEGY),   // 策略
    TOOL("工具", "Agent的工具相关配置", -1, MCP_TOOL, SKILL_TOOL, SYSTEM_TOOL, HTTP_TOOL),       // 工具
    PROMPT("提示词", "Agent的提示词相关配置", -1, SYSTEM_PROMPT, ASSISTANT_PROMPT),     // 提示词
    MODEL("模型", "Agent的模型相关配置", 1, CHAT_MODEL, EMBEDDING_MODEL),       // 模型
    KNOWLEDGE("知识库", "Agent的知识库相关配置", -1, KNOWLEDGE_BASE);   // 知识库

    private final String displayName;
    private final String description;
    private final AgentConfigType[] types;
    /**
     * 可以存在多少个配置，-1表示无限制
     */
    private final int sum;

    AgentConfigCategory(String displayName, String description, int sum, AgentConfigType... types) {
        this.displayName = displayName;
        this.description = description;
        this.sum = sum;
        this.types = types;
    }

}
