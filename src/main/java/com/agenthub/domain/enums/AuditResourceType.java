package com.agenthub.domain.enums;

/**
 * 全局审计日志 - 资源类型
 * <p>覆盖 Agent 全生命周期的所有可审计资源。</p>
 */
public enum AuditResourceType {
    AGENT,
    AGENT_TEAM,
    DATA_SOURCE,
    DATA_SOURCE_SCHEMA,
    PERMISSION_STRATEGY,
    TOOL,
    TOOL_STRATEGY,
    MODEL,
    MODEL_STRATEGY,
    KNOWLEDGE_BASE,
    RETRIEVAL_STRATEGY,
    GUARDRAIL_STRATEGY,
    SKILL,
    WORKFLOW,
    SUBAGENT,
    SESSION,
    TENANT,
    WORKSPACE,
    MEMBER,
    USER,
    VECTOR_STORE,
    PROMPT_TEMPLATE,
    SCHEDULED_TASK,
    TRACE
}
