package com.agenthub.domain.model;

import java.util.Set;

/**
 * 权限定义.
 * <p>
 * 定义系统中所有可用的权限标识。
 * </p>
 */
public final class Permission {
    
    private Permission() {}
    
    // 租户管理权限
    public static final String TENANT_READ = "tenant:read";
    public static final String TENANT_UPDATE = "tenant:update";
    public static final String TENANT_DELETE = "tenant:delete";
    
    // 工作空间权限
    public static final String WORKSPACE_CREATE = "workspace:create";
    public static final String WORKSPACE_READ = "workspace:read";
    public static final String WORKSPACE_UPDATE = "workspace:update";
    public static final String WORKSPACE_DELETE = "workspace:delete";
    
    // 知识库权限
    public static final String KNOWLEDGE_CREATE = "knowledge:create";
    public static final String KNOWLEDGE_READ = "knowledge:read";
    public static final String KNOWLEDGE_UPDATE = "knowledge:update";
    public static final String KNOWLEDGE_DELETE = "knowledge:delete";
    
    // Agent权限
    public static final String AGENT_CREATE = "agent:create";
    public static final String AGENT_READ = "agent:read";
    public static final String AGENT_UPDATE = "agent:update";
    public static final String AGENT_DELETE = "agent:delete";
    public static final String AGENT_PUBLISH = "agent:publish";
    
    // 工具权限
    public static final String TOOL_CREATE = "tool:create";
    public static final String TOOL_READ = "tool:read";
    public static final String TOOL_UPDATE = "tool:update";
    public static final String TOOL_DELETE = "tool:delete";
    public static final String TOOL_INVOKE = "tool:invoke";
    
    // 策略权限
    public static final String STRATEGY_CREATE = "strategy:create";
    public static final String STRATEGY_READ = "strategy:read";
    public static final String STRATEGY_UPDATE = "strategy:update";
    public static final String STRATEGY_DELETE = "strategy:delete";
    
    // 成员管理权限
    public static final String MEMBER_CREATE = "member:create";
    public static final String MEMBER_READ = "member:read";
    public static final String MEMBER_UPDATE = "member:update";
    public static final String MEMBER_DELETE = "member:delete";
    
    // 审计权限
    public static final String AUDIT_READ = "audit:read";
    public static final String AUDIT_EXPORT = "audit:export";
    
    // 账单权限
    public static final String BILLING_READ = "billing:read";
    public static final String BILLING_UPDATE = "billing:update";
    
    /**
     * 获取角色对应的权限集合.
     *
     * @param roleCode 角色编码
     * @return 权限集合
     */
    public static Set<String> getPermissionsForRole(String roleCode) {
        return switch (roleCode) {
            case "OWNER" -> Set.of(
                TENANT_READ, TENANT_UPDATE, TENANT_DELETE,
                WORKSPACE_CREATE, WORKSPACE_READ, WORKSPACE_UPDATE, WORKSPACE_DELETE,
                KNOWLEDGE_CREATE, KNOWLEDGE_READ, KNOWLEDGE_UPDATE, KNOWLEDGE_DELETE,
                AGENT_CREATE, AGENT_READ, AGENT_UPDATE, AGENT_DELETE, AGENT_PUBLISH,
                TOOL_CREATE, TOOL_READ, TOOL_UPDATE, TOOL_DELETE, TOOL_INVOKE,
                STRATEGY_CREATE, STRATEGY_READ, STRATEGY_UPDATE, STRATEGY_DELETE,
                MEMBER_CREATE, MEMBER_READ, MEMBER_UPDATE, MEMBER_DELETE,
                AUDIT_READ, AUDIT_EXPORT,
                BILLING_READ, BILLING_UPDATE
            );
            case "ADMIN" -> Set.of(
                WORKSPACE_READ, WORKSPACE_UPDATE,
                KNOWLEDGE_CREATE, KNOWLEDGE_READ, KNOWLEDGE_UPDATE, KNOWLEDGE_DELETE,
                AGENT_CREATE, AGENT_READ, AGENT_UPDATE, AGENT_DELETE, AGENT_PUBLISH,
                TOOL_CREATE, TOOL_READ, TOOL_UPDATE, TOOL_DELETE, TOOL_INVOKE,
                STRATEGY_CREATE, STRATEGY_READ, STRATEGY_UPDATE, STRATEGY_DELETE,
                MEMBER_CREATE, MEMBER_READ, MEMBER_UPDATE, MEMBER_DELETE,
                AUDIT_READ, AUDIT_EXPORT,
                BILLING_READ
            );
            case "DEVELOPER" -> Set.of(
                WORKSPACE_READ,
                KNOWLEDGE_CREATE, KNOWLEDGE_READ, KNOWLEDGE_UPDATE,
                AGENT_CREATE, AGENT_READ, AGENT_UPDATE, AGENT_PUBLISH,
                TOOL_READ, TOOL_INVOKE,
                STRATEGY_CREATE, STRATEGY_READ, STRATEGY_UPDATE,
                AUDIT_READ
            );
            case "VIEWER" -> Set.of(
                WORKSPACE_READ,
                KNOWLEDGE_READ,
                AGENT_READ,
                TOOL_READ,
                STRATEGY_READ,
                AUDIT_READ
            );
            case "AUDITOR" -> Set.of(
                AUDIT_READ, AUDIT_EXPORT,
                BILLING_READ
            );
            default -> throw new IllegalStateException("Unexpected value: " + roleCode);
        };
    }
}
