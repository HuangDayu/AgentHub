package com.agenthub.application.service;

import com.agenthub.application.command.RateLimitCheckCommand;
import com.agenthub.application.port.out.AuditLogger;
import com.agenthub.application.port.out.DataSourcePermissionPort;
import com.agenthub.application.port.out.repositories.PermissionStrategyRepository;
import com.agenthub.domain.enums.AuditAction;
import com.agenthub.domain.enums.AuditActorType;
import com.agenthub.domain.enums.AuditResourceType;
import com.agenthub.domain.enums.AuditStatus;
import com.agenthub.domain.enums.OperationLevel;
import com.agenthub.domain.event.AuditEvent;
import com.agenthub.domain.exception.DangerousOperationBlockedException;
import com.agenthub.domain.exception.PermissionDeniedException;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.domain.model.PermissionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 数据源权限服务 - 组合 ACL/限流/审计/表权限校验
 */
@Service
@RequiredArgsConstructor
public class DataSourcePermissionService {
    private final PermissionStrategyRepository policyRepository;
    private final DataSourcePermissionPort port;
    private final AuditLogger auditLogger;

    /**
     * 检查权限（无策略不限制）
     */
    public void checkPermission(String userId, AgentDataSource source, Object body) {
        PermissionStrategy policy = resolvePolicy(source);
        if (policy == null) return;
        checkRole(policy, userId, source);
        checkProtocol(policy, source);
        checkOperationLevel(policy, source, body);
        checkDangerousSql(policy, source, body);
        checkRateLimit(policy, userId, source);
    }

    /**
     * 检查用户角色
     */
    private void checkRole(PermissionStrategy policy, String userId, AgentDataSource source) {
        if (policy.getAllowedRoles() == null || policy.getAllowedRoles().isEmpty()) return;
        String userRole = port.getUserRole(userId, source.getWorkspaceId());
        if (!policy.getAllowedRoles().contains(userRole)) {
            throw new PermissionDeniedException("user role " + userRole + " not in allowed list");
        }
    }

    /**
     * 检查协议黑名单
     */
    private void checkProtocol(PermissionStrategy policy, AgentDataSource source) {
        if (policy.getProtocolBlocklist() == null) return;
        if (policy.getProtocolBlocklist().contains(source.getProtocol())) {
            throw new PermissionDeniedException(
                "protocol " + source.getProtocol() + " is blocked by policy");
        }
    }

    /**
     * 检查操作级别
     */
    private void checkOperationLevel(PermissionStrategy policy, AgentDataSource source, Object body) {
        if (policy.getAllowedOperations() == null || policy.getAllowedOperations().isEmpty()) return;
        OperationLevel op = detectOperationLevel(source, body);
        if (!policy.getAllowedOperations().contains(op)) {
            throw new PermissionDeniedException("operation " + op + " not allowed by policy");
        }
    }

    /**
     * 检查危险 SQL
     */
    private void checkDangerousSql(PermissionStrategy policy, AgentDataSource source, Object body) {
        if (!policy.isDangerousSqlBlock() || !isSqlDataSource(source) || body == null) return;
        String dangerous = findDangerousSql(body.toString());
        if (dangerous != null) {
            throw new DangerousOperationBlockedException("dangerous SQL detected: " + dangerous);
        }
    }

    /**
     * 速率限制
     */
    private void checkRateLimit(PermissionStrategy policy, String userId, AgentDataSource source) {
        port.checkRateLimit(new RateLimitCheckCommand(userId, source.getId(),
            policy.getRateLimitPerMinute(), policy.getRateLimitPerHour()));
    }

    /**
     * 记录审计日志（数据源调用）
     */
    public void recordInvoke(DataSourceInvokeContext ctx) {
        auditLogger.logAsync(buildEvent(ctx));
    }

    private AuditEvent buildEvent(DataSourceInvokeContext ctx) {
        AuditEvent event = new AuditEvent();
        event.setId(UUID.randomUUID().toString());
        event.setResourceType(AuditResourceType.DATA_SOURCE);
        event.setAction(AuditAction.INVOKE);
        AgentDataSource source = ctx.getSource();
        event.setResourceId(source.getId());
        event.setResourceName(source.getName());
        event.setActorId(ctx.getUserId());
        event.setActorType(AuditActorType.USER);
        event.setAgentId(ctx.getAgentId());
        event.setSessionId(ctx.getSessionId());
        event.setTenantId(source.getTenantId());
        event.setWorkspaceId(source.getWorkspaceId());
        event.setRequest(ctx.getRequest());
        event.setResponse(ctx.getResponse());
        event.setElapsedMs(ctx.getElapsedMs());
        event.setStatus(AuditStatus.valueOf(ctx.getStatus()));
        event.setErrorMessage(ctx.getErrorMessage());
        event.setMetadata(Map.of("protocol", source.getProtocol().name()));
        return event;
    }

    private PermissionStrategy resolvePolicy(AgentDataSource source) {
        if (source.getPermissionPolicyId() == null) return null;
        return policyRepository.findById(source.getPermissionPolicyId()).orElse(null);
    }

    private OperationLevel detectOperationLevel(AgentDataSource source, Object body) {
        if (body == null) return OperationLevel.READ;
        String s = body.toString().toUpperCase();
        if (s.contains("INSERT") || s.contains("CREATE")) return OperationLevel.CREATE;
        if (s.contains("UPDATE") || s.contains("MODIFY")) return OperationLevel.UPDATE;
        if (s.contains("DELETE") || s.contains("REMOVE") || s.contains("DROP")) return OperationLevel.DELETE;
        return OperationLevel.READ;
    }

    private boolean isSqlDataSource(AgentDataSource source) {
        return source.getProtocol() != null
            && Set.of("JDBC", "SQL").contains(source.getProtocol().name());
    }

    private String findDangerousSql(String body) {
        String upper = body.toUpperCase();
        for (String kw : new String[]{"DROP TABLE", "TRUNCATE", "DELETE FROM", "UPDATE ", "ALTER TABLE"}) {
            if (upper.contains(kw)) return kw;
        }
        return null;
    }
}
