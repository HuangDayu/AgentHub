package com.agenthub.domain.event;

import com.agenthub.domain.enums.AuditAction;
import com.agenthub.domain.enums.AuditActorType;
import com.agenthub.domain.enums.AuditResourceType;
import com.agenthub.domain.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 全局审计事件 - 覆盖 Agent 全生命周期
 * <p>任何资源类型（AGENT/TOOL/MODEL/...）的 CRUD/调用/启用/发布等操作均通过此事件记录。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String actorId;
    private AuditActorType actorType;
    private String agentId;
    private String sessionId;
    private AuditResourceType resourceType;
    private String resourceId;
    private String resourceName;
    private AuditAction action;
    private AuditStatus status;
    private Object request;
    private Object response;
    private String errorMessage;
    private Map<String, Object> metadata;
    private Long elapsedMs;
    private Instant createdAt;
}
