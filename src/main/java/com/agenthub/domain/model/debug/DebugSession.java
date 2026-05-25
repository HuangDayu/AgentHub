package com.agenthub.domain.model.debug;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * DebugSession 领域模型.
 * 表示调试会话.
 */
@Data
public class DebugSession {
    private String sessionId;
    private String runId;
    private String agentId;

    private String status;

    private List<Map<String, Object>> breakpoints;
    private Map<String, Object> currentState;

    private String tenantId;
    private String workspaceId;
    private Instant createdAt;
    private Instant updatedAt;

    public DebugSession() {
        this.sessionId = randomId();
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static DebugSession create(String runId, String agentId) {
        DebugSession session = new DebugSession();
        session.runId = runId;
        session.agentId = agentId;
        return session;
    }

    public DebugSession interrupt() {
        this.status = "INTERRUPTED";
        this.updatedAt = Instant.now();
        return this;
    }

    public DebugSession resume() {
        this.status = "RUNNING";
        this.updatedAt = Instant.now();
        return this;
    }
}
