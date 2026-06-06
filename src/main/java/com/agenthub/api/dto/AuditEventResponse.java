package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {
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
}
