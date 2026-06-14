package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.CreateAgentDataSourceCommand;
import com.agenthub.application.command.InvokeAgentDataSourceCommand;
import com.agenthub.application.command.UpdateAgentDataSourceCommand;
import com.agenthub.application.dto.AgentDataSourceOutput;
import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.enums.AgentDataSourceStatus;
import com.agenthub.domain.event.AgentDataSourceChangedEvent;
import com.agenthub.domain.exception.AgentDataSourceConflictException;
import com.agenthub.domain.exception.AgentDataSourceNotFoundException;
import com.agenthub.domain.exception.AgentDataSourceValidationException;
import com.agenthub.domain.model.data_source.AgentDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Agent 数据源用例 - CRUD + 启用/禁用 + 测试/调用
 */
@Component
@RequiredArgsConstructor
public class AgentDataSourceUseCase {
    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 列出工作空间下所有数据源
     */
    public List<AgentDataSourceOutput> list(String workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
            .map(AgentDataSourceOutput::from)
            .toList();
    }

    /**
     * 详情
     */
    public AgentDataSourceOutput get(String id) {
        return AgentDataSourceOutput.from(requireSource(id));
    }

    /**
     * 创建（默认 disabled）
     */
    public AgentDataSourceOutput create(CreateAgentDataSourceCommand cmd) {
        validateCommand(cmd);
        checkNameUnique(cmd);
        AgentDataSource source = newSource(cmd);
        AgentDataSource saved = repository.save(source);
        publishChange(saved.getId(), saved.getWorkspaceId(), AgentDataSourceChangedEvent.ChangeType.CREATED);
        return AgentDataSourceOutput.from(saved);
    }

    /**
     * 更新（不切换 enabled）
     */
    public AgentDataSourceOutput update(String id, UpdateAgentDataSourceCommand cmd) {
        AgentDataSource source = applyUpdate(requireSource(id), cmd);
        AgentDataSource saved = repository.save(source);
        publishChange(saved.getId(), saved.getWorkspaceId(), AgentDataSourceChangedEvent.ChangeType.UPDATED);
        return AgentDataSourceOutput.from(saved);
    }

    /**
     * 启用
     */
    public AgentDataSourceOutput enable(String id) {
        AgentDataSource source = markEnabled(requireSource(id));
        AgentDataSource saved = repository.save(source);
        saved = bootstrapOrMarkError(saved);
        publishChange(id, source.getWorkspaceId(), AgentDataSourceChangedEvent.ChangeType.ENABLED);
        return AgentDataSourceOutput.from(saved);
    }

    /**
     * 禁用
     */
    public AgentDataSourceOutput disable(String id) {
        AgentDataSource source = markDisabled(requireSource(id));
        AgentDataSource saved = repository.save(source);
        safeShutdown(id);
        publishChange(id, source.getWorkspaceId(), AgentDataSourceChangedEvent.ChangeType.DISABLED);
        return AgentDataSourceOutput.from(saved);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        AgentDataSource source = requireSource(id);
        safeShutdown(id);
        repository.deleteById(id);
        publishChange(id, source.getWorkspaceId(), AgentDataSourceChangedEvent.ChangeType.DELETED);
    }

    /**
     * 测试连接
     */
    public AgentDataSourcePort.AgentDataSourceTestResult test(String id) {
        return port.test(requireSource(id));
    }

    /**
     * 调用数据源
     */
    public AgentDataSourcePort.AgentDataSourceInvokeResult invoke(String id, InvokeAgentDataSourceCommand cmd) {
        AgentDataSource source = requireSource(id);
        ensureEnabled(source);
        return invokeSafely(source, cmd);
    }

    private void ensureEnabled(AgentDataSource source) {
        if (!source.isEnabled()) {
            throw new AgentDataSourceValidationException("data source is disabled: " + source.getId());
        }
    }

    private AgentDataSourcePort.AgentDataSourceInvokeResult invokeSafely(AgentDataSource source, InvokeAgentDataSourceCommand cmd) {
        try {
            return port.invoke(source, cmd.getHeaders(), cmd.getBody());
        } catch (RuntimeException e) {
            return failedInvokeResult(e.getMessage());
        }
    }

    private AgentDataSourcePort.AgentDataSourceInvokeResult failedInvokeResult(String message) {
        AgentDataSourcePort.AgentDataSourceInvokeResult err = new AgentDataSourcePort.AgentDataSourceInvokeResult();
        err.setSuccess(false);
        err.setErrorMessage(message);
        return err;
    }

    /**
     * 列出所有支持的协议描述符
     */
    public List<?> listProtocols() {
        return port.listDescriptors();
    }

    private AgentDataSource requireSource(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new AgentDataSourceNotFoundException(id));
    }

    private void checkNameUnique(CreateAgentDataSourceCommand cmd) {
        if (repository.existsByWorkspaceIdAndName(cmd.getWorkspaceId(), cmd.getName())) {
            throw new AgentDataSourceConflictException("name already exists: " + cmd.getName());
        }
    }

    private AgentDataSource newSource(CreateAgentDataSourceCommand cmd) {
        AgentDataSource source = BeanUtil.copyProperties(cmd, AgentDataSource.class);
        source.setId(UUID.randomUUID().toString());
        source.setProtocol(AgentDataSourceProtocol.valueOf(cmd.getProtocol()));
        source.setEnabled(false);
        source.setStatus(AgentDataSourceStatus.DISABLED);
        source.setCreatedAt(Instant.now());
        source.setUpdatedAt(Instant.now());
        return source;
    }

    private AgentDataSource applyUpdate(AgentDataSource source, UpdateAgentDataSourceCommand cmd) {
        if (cmd.getDescription() != null) source.setDescription(cmd.getDescription());
        if (cmd.getEndpointUri() != null) source.setEndpointUri(cmd.getEndpointUri());
        if (cmd.getPropertiesJson() != null) source.setPropertiesJson(cmd.getPropertiesJson());
        if (cmd.getPermissionPolicyId() != null) source.setPermissionPolicyId(cmd.getPermissionPolicyId());
        if (cmd.getSchemaId() != null) source.setSchemaId(cmd.getSchemaId());
        source.setUpdatedBy(cmd.getUpdatedBy());
        source.setUpdatedAt(Instant.now());
        return source;
    }

    private AgentDataSource markEnabled(AgentDataSource source) {
        source.setEnabled(true);
        source.setStatus(AgentDataSourceStatus.ENABLED);
        source.setUpdatedAt(Instant.now());
        return source;
    }

    private AgentDataSource markDisabled(AgentDataSource source) {
        source.setEnabled(false);
        source.setStatus(AgentDataSourceStatus.DISABLED);
        source.setUpdatedAt(Instant.now());
        return source;
    }

    private AgentDataSource bootstrapOrMarkError(AgentDataSource saved) {
        try {
            port.bootstrap(saved);
            return saved;
        } catch (Exception e) {
            saved.setStatus(AgentDataSourceStatus.ERROR);
            saved.setLastErrorMessage(e.getMessage());
            return repository.save(saved);
        }
    }

    private void safeShutdown(String id) {
        try {
            port.shutdown(id);
        } catch (Exception ignored) {
            // 关闭失败不阻塞后续流程
        }
    }

    private void publishChange(String sourceId, String workspaceId,
                                AgentDataSourceChangedEvent.ChangeType type) {
        eventPublisher.publishEvent(new AgentDataSourceChangedEvent(sourceId, workspaceId, type));
    }

    private void validateCommand(CreateAgentDataSourceCommand cmd) {
        requireField(cmd.getName() != null && !cmd.getName().isBlank(), "name");
        requireField(cmd.getProtocol() != null, "protocol");
        requireField(cmd.getEndpointUri() != null && !cmd.getEndpointUri().isBlank(), "endpointUri");
    }

    private void requireField(boolean valid, String fieldName) {
        if (!valid) throw new AgentDataSourceValidationException(fieldName + " is required");
    }
}
