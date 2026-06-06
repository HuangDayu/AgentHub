package com.agenthub.api.controller;

import com.agenthub.api.dto.AuditEventResponse;
import com.agenthub.api.dto.AuditLogQueryParams;
import com.agenthub.api.mapper.AgentDataSourceViewMapper;
import com.agenthub.application.dto.AuditEventOutput;
import com.agenthub.application.port.out.repositories.AuditLogRepository;
import com.agenthub.application.usecase.AuditLogUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 全局审计日志 Controller - 租户级
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
    private final AuditLogUseCase useCase;

    public AuditLogController(AuditLogUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * 查询审计日志（支持多维度过滤 + 分页）
     */
    @GetMapping
    public Map<String, Object> query(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @org.springframework.web.bind.annotation.ModelAttribute AuditLogQueryParams params) {
        AuditLogRepository.AuditLogQuery q = toQuery(tenantId, params);
        return buildResult(q, params.getPage(), params.getSize());
    }

    /**
     * 列出支持的资源类型
     */
    @GetMapping("/resource-types")
    public List<String> resourceTypes() {
        return useCase.listResourceTypes();
    }

    /**
     * 列出支持的动作
     */
    @GetMapping("/actions")
    public List<String> actions() {
        return useCase.listActions();
    }

    private AuditLogRepository.AuditLogQuery toQuery(String tenantId, AuditLogQueryParams p) {
        AuditLogRepository.AuditLogQuery q = new AuditLogRepository.AuditLogQuery();
        q.setTenantId(tenantId);
        q.setWorkspaceId(p.getWorkspaceId());
        q.setResourceType(p.getResourceType());
        q.setResourceId(p.getResourceId());
        q.setActorId(p.getActorId());
        q.setAction(p.getAction());
        q.setStatus(p.getStatus());
        q.setFrom(p.getFrom());
        q.setTo(p.getTo());
        q.setPage(p.getPage());
        q.setSize(p.getSize());
        return q;
    }

    private Map<String, Object> buildResult(AuditLogRepository.AuditLogQuery q, int page, int size) {
        List<AuditEventResponse> items = useCase.query(q).stream()
                .map(AgentDataSourceViewMapper::toResponse)
                .toList();
        long total = useCase.count(q);
        return Map.of("items", items, "total", total, "page", page, "size", size);
    }
}
