package com.agenthub.domain.model.monitor;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * Alert 领域模型.
 * 表示告警信息.
 */
@Data
public class Alert {
    private String id;
    private String alertLevel;
    private String alertType;

    private String title;
    private String message;

    private String runId;
    private String agentId;
    private String traceId;

    private Map<String, Object> metadata;

    private boolean resolved;
    private Instant resolvedAt;
    private String resolvedBy;

    private String tenantId;
    private String workspaceId;
    private Instant createdAt;

    public Alert() {
        this.id = randomId();
        this.resolved = false;
        this.createdAt = Instant.now();
    }

    public static Alert create(String level, String type, String title, String message) {
        Alert alert = new Alert();
        alert.alertLevel = level;
        alert.alertType = type;
        alert.title = title;
        alert.message = message;
        return alert;
    }

    public Alert resolve(String resolvedBy) {
        this.resolved = true;
        this.resolvedAt = Instant.now();
        this.resolvedBy = resolvedBy;
        return this;
    }
}
