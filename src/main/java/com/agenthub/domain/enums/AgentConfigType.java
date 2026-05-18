package com.agenthub.domain.enums;

import lombok.Getter;

/**
 * @author huangdayu
 */
@Getter
public enum AgentConfigType {

    ALL_TYPE("所有类型", "所有类型配置"),
    RETRIEVAL_STRATEGY("检索策略", "知识检索策略配置"),      // 检索策略
    TOOL_STRATEGY("工具策略", "工具调用策略配置"),  // 工具策略
    MODEL_STRATEGY("模型策略", "模型调用策略配置"), // 模型策略
    GUARDRAIL_STRATEGY("护栏策略", "输入输出护栏策略"),      // 护栏策略
    SYSTEM_PROMPT("系统提示词", "Agent系统提示词模板"),  // 系统提示词
    ASSISTANT_PROMPT("助理提示词", "Agent助理提示词模板"), // 助手提示词
    CHAT_MODEL("聊天模型", "对话模型配置"), // 聊天模型
    EMBEDDING_MODEL("嵌入模型", "向量嵌入模型配置"), // 嵌入模型
    KNOWLEDGE_BASE("知识库", "知识库配置"),   // 知识库
    KNOWLEDGE_WIKI("Wiki", "Wiki配置"),   // 知识库
    MCP_TOOL("MCP", "MCP协议工具配置"),        // MCP工具
    SKILL_TOOL("Skill", "Skill技能配置"),        // Skill工具
    SYSTEM_TOOL("System", "System工具配置"),        // Function工具
    HTTP_TOOL("Http", "Http工具配置");       // HTTP工具

    private final String displayName;
    private final String description;

    AgentConfigType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
