package com.agenthub.application.usecase;

import com.agenthub.application.dto.AuditEventOutput;
import com.agenthub.application.port.out.repositories.AuditLogRepository;
import com.agenthub.domain.enums.AuditAction;
import com.agenthub.domain.enums.AuditResourceType;
import com.agenthub.domain.event.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 审计日志查询用例 - 全局
 */
@Component
@RequiredArgsConstructor
public class AuditLogUseCase {
    private final AuditLogRepository repository;

    /**
     * 分页查询审计日志（租户级）
     */
    public List<AuditEventOutput> query(AuditLogRepository.AuditLogQuery query) {
        List<AuditEvent> events = repository.query(query);
        return events.stream().map(AuditEventOutput::from).toList();
    }

    /**
     * 统计
     */
    public long count(AuditLogRepository.AuditLogQuery query) {
        return repository.count(query);
    }

    /**
     * 列出所有支持的资源类型
     */
    public List<String> listResourceTypes() {
        return Arrays.stream(AuditResourceType.values()).map(Enum::name).toList();
    }

    /**
     * 列出所有支持的动作
     */
    public List<String> listActions() {
        return Arrays.stream(AuditAction.values()).map(Enum::name).toList();
    }
}
