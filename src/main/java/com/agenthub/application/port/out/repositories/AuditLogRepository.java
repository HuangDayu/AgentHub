package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.event.AuditEvent;

import java.time.Instant;
import java.util.List;

/**
 * 全局审计日志仓储端口
 */
public interface AuditLogRepository {
    void save(AuditEvent event);
    void saveAll(List<AuditEvent> events);
    List<AuditEvent> query(AuditLogQuery query);
    long count(AuditLogQuery query);

    /**
     * 审计日志查询条件
     */
    class AuditLogQuery {
        private String tenantId;
        private String workspaceId;
        private String resourceType;
        private String resourceId;
        private String actorId;
        private String action;
        private String status;
        private Instant from;
        private Instant to;
        private int page;
        private int size;

        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public String getResourceId() { return resourceId; }
        public void setResourceId(String resourceId) { this.resourceId = resourceId; }
        public String getActorId() { return actorId; }
        public void setActorId(String actorId) { this.actorId = actorId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Instant getFrom() { return from; }
        public void setFrom(Instant from) { this.from = from; }
        public Instant getTo() { return to; }
        public void setTo(Instant to) { this.to = to; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
}
