package com.agenthub.infrastructure.audit;

import com.agenthub.application.annotation.Audited;
import com.agenthub.application.port.out.AuditLogger;
import com.agenthub.domain.enums.AuditAction;
import com.agenthub.domain.enums.AuditActorType;
import com.agenthub.domain.enums.AuditResourceType;
import com.agenthub.domain.enums.AuditStatus;
import com.agenthub.domain.event.AuditEvent;
import com.agenthub.domain.exception.PermissionDeniedException;
import com.agenthub.domain.exception.RateLimitExceededException;
import com.agenthub.infrastructure.context.TenantContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 审计切面 - 拦截 @Audited 注解自动写入全局审计日志
 */
@Aspect
@Component
public class AuditAspect {
    private final AuditLogger auditLogger;

    public AuditAspect(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    /**
     * 拦截 @Audited 注解的方法
     */
    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        long start = System.currentTimeMillis();
        AuditEvent event = newAuditEvent(audited, resolveAction(audited.action(), pjp));
        Object result = runOrFail(pjp, event, start);
        markSuccess(event, audited.includeResult() ? result : null, start);
        return result;
    }

    private Object runOrFail(ProceedingJoinPoint pjp, AuditEvent event, long start) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Exception e) {
            onFailure(event, e, start);
            throw e;
        }
    }

    private void markSuccess(AuditEvent event, Object response, long start) {
        event.setStatus(AuditStatus.SUCCESS);
        event.setElapsedMs(System.currentTimeMillis() - start);
        if (response != null) event.setResponse(response);
        auditLogger.logAsync(event);
    }

    private void onFailure(AuditEvent event, Exception e, long start) {
        event.setStatus(mapStatus(e));
        event.setErrorMessage(e.getMessage());
        event.setElapsedMs(System.currentTimeMillis() - start);
        auditLogger.logAsync(event);
    }

    private AuditEvent newAuditEvent(Audited audited, String action) {
        AuditEvent e = baseEvent(audited, action);
        e.setCreatedAt(Instant.now());
        e.setMetadata(Map.of("method", "UseCase"));
        attachTenantContext(e);
        return e;
    }

    private AuditEvent baseEvent(Audited audited, String action) {
        AuditEvent e = new AuditEvent();
        e.setId(UUID.randomUUID().toString());
        e.setResourceType(AuditResourceType.valueOf(audited.resourceType()));
        e.setAction(AuditAction.valueOf(action));
        e.setActorType(AuditActorType.USER);
        return e;
    }

    private void attachTenantContext(AuditEvent e) {
        TenantContextHolder.current().ifPresent(context -> {
            e.setTenantId(context.getTenantId());
            e.setWorkspaceId(context.getWorkspaceId());
            e.setActorId(context.getRequestId());
        });
    }

    private String resolveAction(String action, ProceedingJoinPoint pjp) {
        if (action != null && !action.isBlank()) return action.toUpperCase();
        String name = pjp.getSignature().getName().toLowerCase();
        return mapNameToAction(name);
    }

    private String mapNameToAction(String name) {
        if (matchesAny(name, "create", "add")) return "CREATE";
        if (matchesAny(name, "update", "modify")) return "UPDATE";
        if (matchesAny(name, "delete", "remove")) return "DELETE";
        if (matchesAny(name, "invoke", "call", "execute")) return "INVOKE";
        return matchSinglePrefix(name);
    }

    private String matchSinglePrefix(String name) {
        if (name.startsWith("enable")) return "ENABLE";
        if (name.startsWith("disable")) return "DISABLE";
        if (name.startsWith("test")) return "TEST";
        if (name.startsWith("publish")) return "PUBLISH";
        return "READ";
    }

    private boolean matchesAny(String name, String... prefixes) {
        for (String p : prefixes) {
            if (name.startsWith(p)) return true;
        }
        return false;
    }

    private AuditStatus mapStatus(Exception e) {
        if (e instanceof PermissionDeniedException || e instanceof RateLimitExceededException) {
            return AuditStatus.DENIED;
        }
        return AuditStatus.FAILED;
    }
}
