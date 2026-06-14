package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.UpsertPermissionStrategyCommand;
import com.agenthub.application.dto.PermissionStrategyOutput;
import com.agenthub.application.port.out.repositories.PermissionStrategyRepository;
import com.agenthub.domain.exception.AgentDataSourceConflictException;
import com.agenthub.domain.exception.AgentDataSourceNotFoundException;
import com.agenthub.domain.model.strategy.PermissionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 权限策略用例 - 第 5 个策略
 */
@Component
@RequiredArgsConstructor
public class PermissionStrategyUseCase {
    private final PermissionStrategyRepository repository;

    /**
     * 列出工作空间下所有策略
     */
    public List<PermissionStrategyOutput> list(String workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
            .map(PermissionStrategyOutput::from)
            .toList();
    }

    /**
     * 详情
     */
    public PermissionStrategyOutput get(String id) {
        return PermissionStrategyOutput.from(requirePolicy(id));
    }

    /**
     * 创建或更新
     */
    public PermissionStrategyOutput upsert(UpsertPermissionStrategyCommand cmd) {
        validate(cmd);
        boolean isCreate = cmd.getId() == null || cmd.getId().isBlank();
        PermissionStrategy policy = isCreate ? newPolicy(cmd) : requirePolicy(cmd.getId());
        applyCommand(policy, cmd);
        PermissionStrategy saved = repository.save(policy);
        return PermissionStrategyOutput.from(saved);
    }

    private PermissionStrategy newPolicy(UpsertPermissionStrategyCommand cmd) {
        ensureNameAvailable(cmd);
        PermissionStrategy policy = new PermissionStrategy();
        policy.setId(UUID.randomUUID().toString());
        policy.setCreatedAt(Instant.now());
        return initDefaults(policy);
    }

    private PermissionStrategy initDefaults(PermissionStrategy policy) {
        policy.setAuditLogEnabled(true);
        policy.setAuditLogRetentionDays(90);
        return policy;
    }

    private void ensureNameAvailable(UpsertPermissionStrategyCommand cmd) {
        if (repository.existsByWorkspaceIdAndName(cmd.getWorkspaceId(), cmd.getName())) {
            throw new AgentDataSourceConflictException("policy name exists: " + cmd.getName());
        }
    }

    private void applyCommand(PermissionStrategy policy, UpsertPermissionStrategyCommand cmd) {
        BeanUtil.copyProperties(cmd, policy, "id", "tenantId", "workspaceId", "createdAt");
        policy.setUpdatedAt(Instant.now());
    }

    /**
     * 删除
     */
    public void delete(String id) {
        requirePolicy(id);
        repository.deleteById(id);
    }

    private PermissionStrategy requirePolicy(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new AgentDataSourceNotFoundException("permission strategy not found: " + id));
    }

    private void validate(UpsertPermissionStrategyCommand cmd) {
        if (cmd.getName() == null || cmd.getName().isBlank()) {
            throw new com.agenthub.domain.exception.AgentDataSourceValidationException("name is required");
        }
        if (cmd.getWorkspaceId() == null || cmd.getWorkspaceId().isBlank()) {
            throw new com.agenthub.domain.exception.AgentDataSourceValidationException("workspaceId is required");
        }
    }
}
