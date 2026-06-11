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
import com.agenthub.infrastructure.context.TenantThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 审计切面 - 自动拦截 UseCase 层所有 public 方法，写入全局审计日志。
 * <p>
 * 默认行为：拦截 {@code com.agenthub.application.usecase..*} 包下所有 public 方法，
 * 从类名推断 resourceType，从方法名推断 action。
 * </p>
 * <p>
 * 覆盖行为：方法上标注 {@link Audited} 时，使用注解的 resourceType/action/includeResult。
 * </p>
 */
@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogger auditLogger;

    public AuditAspect(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    /**
     * 自动拦截 UseCase 层所有 public 方法。
     */
    @Around("execution(* com.agenthub.application.usecase..*.*(..))")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        if (shouldSkip(method)) return pjp.proceed();
        return runOrLog(pjp, buildEvent(pjp, method));
    }

    private boolean shouldSkip(Method method) {
        return method.isAnnotationPresent(com.agenthub.common.annotations.IgnoreTenantContext.class);
    }

    private AuditEvent buildEvent(ProceedingJoinPoint pjp, Method method) {
        long start = System.currentTimeMillis();
        Audited audited = method.getAnnotation(Audited.class);
        return audited != null
                ? buildFromAnnotation(audited, pjp, method)
                : buildFromInference(pjp, method, start);
    }

    private AuditEvent buildFromAnnotation(Audited audited, ProceedingJoinPoint pjp,
                                            Method method) {
        long start = System.currentTimeMillis();
        AuditEvent e = newEvent(pjp, method, start);
        e.setResourceType(AuditResourceType.valueOf(audited.resourceType()));
        e.setAction(AuditAction.valueOf(resolveAction(audited.action(), method)));
        return e;
    }

    private AuditEvent buildFromInference(ProceedingJoinPoint pjp, Method method, long start) {
        AuditEvent e = newEvent(pjp, method, start);
        e.setResourceType(inferResourceType(pjp));
        e.setAction(AuditAction.valueOf(inferAction(method)));
        captureRequestParams(e, pjp, method);
        extractResourceId(e, pjp, method);
        return e;
    }

    private AuditResourceType inferResourceType(ProceedingJoinPoint pjp) {
        String simpleName = pjp.getTarget().getClass().getSimpleName();
        String enumName = toUpperSnake(simpleName.replace("UseCase", ""));
        try {
            return AuditResourceType.valueOf(enumName);
        } catch (IllegalArgumentException ex) {
            log.warn("无法推断 resourceType: {} → {}，默认使用 AGENT", simpleName, enumName);
            return AuditResourceType.AGENT;
        }
    }

    private String inferAction(Method method) {
        return mapNameToAction(method.getName().toLowerCase());
    }

    private void captureRequestParams(AuditEvent e, ProceedingJoinPoint pjp, Method method) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) return;
        Parameter[] params = method.getParameters();
        Map<String, Object> paramMap = new LinkedHashMap<>();
        for (int i = 0; i < params.length && i < args.length; i++) {
            String name = params[i].isNamePresent() ? params[i].getName() : "arg" + i;
            paramMap.put(name, sanitize(args[i]));
        }
        e.setRequest(paramMap);
    }

    private void extractResourceId(AuditEvent e, ProceedingJoinPoint pjp, Method method) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) return;
        String id = extractIdFromMethodName(method, args);
        if (id != null) {
            e.setResourceId(id);
        } else {
            extractIdFromFirstArg(e, args[0]);
        }
    }

    private String extractIdFromMethodName(Method method, Object[] args) {
        String name = method.getName().toLowerCase();
        if (!name.contains("id") && !name.contains("by")) return null;
        for (Object arg : args) {
            if (arg instanceof String id && id.length() <= 64) return id;
        }
        return null;
    }

    private void extractIdFromFirstArg(AuditEvent e, Object first) {
        if (first == null) return;
        try {
            Method getId = first.getClass().getMethod("getId");
            Object id = getId.invoke(first);
            if (id instanceof String s) e.setResourceId(s);
        } catch (Exception ignored) {
        }
    }

    private AuditEvent newEvent(ProceedingJoinPoint pjp, Method method, long start) {
        AuditEvent e = new AuditEvent();
        e.setId(UUID.randomUUID().toString());
        e.setActorType(AuditActorType.USER);
        e.setCreatedAt(Instant.now());
        e.setElapsedMs(System.currentTimeMillis() - start);
        attachTenantContext(e);
        e.setMetadata(buildMetadata(pjp, method));
        return e;
    }

    private void attachTenantContext(AuditEvent e) {
        TenantContextHolder.current().ifPresent(ctx -> {
            e.setTenantId(ctx.getTenantId());
            e.setWorkspaceId(ctx.getWorkspaceId());
            e.setActorId(ctx.getUserId());
            e.setAgentId(ctx.getAgentId());
            e.setSessionId(ctx.getSessionId());
        });
    }

    private Map<String, Object> buildMetadata(ProceedingJoinPoint pjp, Method method) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("class", pjp.getTarget().getClass().getSimpleName());
        meta.put("method", method.getName());
        meta.put("paramTypes", paramTypeNames(method));
        return meta;
    }

    private Object runOrLog(ProceedingJoinPoint pjp, AuditEvent event) throws Throwable {
        try {
            Object result = pjp.proceed();
            logSuccess(event, result);
            return result;
        } catch (Exception e) {
            logFailure(event, e);
            throw e;
        }
    }

    private void logSuccess(AuditEvent event, Object result) {
        event.setStatus(AuditStatus.SUCCESS);
        auditLogger.logAsync(event);
    }

    private void logFailure(AuditEvent event, Exception e) {
        event.setStatus(mapStatus(e));
        event.setErrorMessage(e.getMessage());
        auditLogger.logAsync(event);
    }

    private String toUpperSnake(String camel) {
        return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

    private String resolveAction(String action, Method method) {
        if (action != null && !action.isBlank()) return action.toUpperCase();
        return inferAction(method);
    }

    private String mapNameToAction(String name) {
        if (name.startsWith("create") || name.startsWith("add") || name.startsWith("install")) return "CREATE";
        if (name.startsWith("update") || name.startsWith("modify") || name.startsWith("save") || name.startsWith("patch")) return "UPDATE";
        if (name.startsWith("delete") || name.startsWith("remove")) return "DELETE";
        if (name.startsWith("invoke") || name.startsWith("call") || name.startsWith("execute") || name.startsWith("run") || name.startsWith("process")) return "EXECUTE";
        if (name.startsWith("enable") || name.startsWith("activate")) return "ENABLE";
        if (name.startsWith("disable") || name.startsWith("deactivate")) return "DISABLE";
        if (name.startsWith("publish")) return "PUBLISH";
        if (name.startsWith("unpublish")) return "UNPUBLISH";
        if (name.startsWith("test") || name.startsWith("ping")) return "TEST";
        if (name.startsWith("sync")) return "SYNC";
        if (name.startsWith("upload")) return "UPLOAD";
        if (name.startsWith("stream")) return "STREAM";
        if (name.startsWith("start")) return "START";
        if (name.startsWith("stop")) return "STOP";
        if (name.startsWith("cancel")) return "CANCEL";
        if (name.startsWith("complete")) return "COMPLETE";
        if (name.startsWith("fail")) return "FAIL";
        if (name.startsWith("resolve")) return "RESOLVE";
        if (name.startsWith("login")) return "LOGIN";
        if (name.startsWith("logout")) return "LOGOUT";
        if (name.startsWith("refresh")) return "REFRESH";
        if (name.startsWith("filter")) return "FILTER";
        if (name.startsWith("validate")) return "VALIDATE";
        if (name.startsWith("enrich")) return "ENRICH";
        if (name.startsWith("close")) return "CLOSE";
        if (name.startsWith("evict")) return "EVICT";
        if (name.startsWith("init") || name.startsWith("initialize")) return "INIT";
        return "READ";
    }

    private AuditStatus mapStatus(Exception e) {
        if (e instanceof PermissionDeniedException || e instanceof RateLimitExceededException) {
            return AuditStatus.DENIED;
        }
        return AuditStatus.FAILED;
    }

    private Object sanitize(Object obj) {
        if (obj == null) return null;
        String name = obj.getClass().getSimpleName().toLowerCase();
        if (name.contains("password") || name.contains("secret") || name.contains("token")) {
            return "****";
        }
        return obj;
    }

    private String paramTypeNames(Method method) {
        StringBuilder sb = new StringBuilder();
        for (Parameter p : method.getParameters()) {
            if (!sb.isEmpty()) sb.append(",");
            sb.append(p.getType().getSimpleName());
        }
        return sb.toString();
    }
}
