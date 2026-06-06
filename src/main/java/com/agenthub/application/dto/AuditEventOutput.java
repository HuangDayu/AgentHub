package com.agenthub.application.dto;

import com.agenthub.domain.event.AuditEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 审计事件输出
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventOutput {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String actorId;
    private String actorType;
    private String agentId;
    private String sessionId;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String action;
    private String status;
    private Object request;
    private Object response;
    private String errorMessage;
    private Map<String, Object> metadata;
    private Long elapsedMs;
    private Instant createdAt;

    public static AuditEventOutput from(AuditEvent e) {
        if (e == null) return null;
        AuditEventOutput o = new AuditEventOutput();
        cn.hutool.core.bean.BeanUtil.copyProperties(e, o);
        o.setActorType(e.getActorType() != null ? e.getActorType().name() : null);
        o.setResourceType(e.getResourceType() != null ? e.getResourceType().name() : null);
        o.setAction(e.getAction() != null ? e.getAction().name() : null);
        o.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        return o;
    }
}
