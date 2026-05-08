package com.agenthub.domain.model;

import lombok.Getter;

import static com.agenthub.domain.model.AgentConfigType.*;
import static com.agenthub.domain.model.AgentConfigType.ASSISTANT_PROMPT;
import static com.agenthub.domain.model.AgentConfigType.CHAT_MODEL;
import static com.agenthub.domain.model.AgentConfigType.EMBEDDING_MODEL;
import static com.agenthub.domain.model.AgentConfigType.GUARDRAIL_STRATEGY;
import static com.agenthub.domain.model.AgentConfigType.HTTP_TOOL;
import static com.agenthub.domain.model.AgentConfigType.KNOWLEDGE_BASE;
import static com.agenthub.domain.model.AgentConfigType.MCP_TOOL;
import static com.agenthub.domain.model.AgentConfigType.SKILL_TOOL;
import static com.agenthub.domain.model.AgentConfigType.SYSTEM_PROMPT;
import static com.agenthub.domain.model.AgentConfigType.SYSTEM_TOOL;

/**
 * @author huangdayu
 */
@Getter
public enum AgentConfigCategory {

    STRATEGY("策略", "Agent的策略相关配置", RETRIEVAL_STRATEGY, TOOL_STRATEGY, MODEL_STRATEGY, GUARDRAIL_STRATEGY),   // 策略
    TOOL("工具", "Agent的工具相关配置", MCP_TOOL, SKILL_TOOL, SYSTEM_TOOL, HTTP_TOOL),       // 工具
    PROMPT("提示词", "Agent的提示词相关配置", SYSTEM_PROMPT, ASSISTANT_PROMPT),     // 提示词
    MODEL("模型", "Agent的模型相关配置", CHAT_MODEL, EMBEDDING_MODEL),       // 模型
    KNOWLEDGE("知识库", "Agent的知识库相关配置", KNOWLEDGE_BASE);   // 知识库

    private final String displayName;
    private final String description;
    private final AgentConfigType[] types;

    AgentConfigCategory(String displayName, String description, AgentConfigType... types) {
        this.displayName = displayName;
        this.description = description;
        this.types = types;
    }

}
