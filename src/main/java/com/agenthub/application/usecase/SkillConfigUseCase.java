package com.agenthub.application.usecase;

import com.agenthub.application.command.CreateSkillConfigCommand;
import com.agenthub.application.dto.SkillConfigOutput;
import com.agenthub.application.port.out.repositories.SkillConfigRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.skill.SkillConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 技能配置用例。
 */
@Component
@RequiredArgsConstructor
public class SkillConfigUseCase {

    private final SkillConfigRepository repository;

    /**
     * 创建配置。
     */
    @Transactional
    public SkillConfigOutput create(CreateSkillConfigCommand command) {
        SkillConfig config = SkillConfig.create(command.getTenantId(), command.getWorkspaceId(),
                command.getName(), command.getSkillPaths());
        config.setDescription(command.getDescription());
        config.setSyncEnabled(command.isSyncEnabled());
        config.setSyncInterval(command.getSyncInterval());
        config.setAutoSync(command.isAutoSync());
        SkillConfig saved = repository.saveOrUpdate(config);
        return toOutput(saved);
    }

    /**
     * 获取配置。
     */
    @Transactional(readOnly = true)
    public SkillConfigOutput get(String configId) {
        SkillConfig config = repository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Skill config not found: " + configId));
        return toOutput(config);
    }

    /**
     * 列出工作空间的所有配置。
     */
    @Transactional(readOnly = true)
    public List<SkillConfigOutput> list(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId).stream()
                .map(this::toOutput)
                .toList();
    }

    /**
     * 更新配置。
     */
    @Transactional
    public SkillConfigOutput update(String configId, CreateSkillConfigCommand command) {
        SkillConfig existing = repository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Skill config not found: " + configId));
        existing.update(command.getName(), command.getDescription(),
                command.getSkillPaths(), command.isSyncEnabled(),
                command.getSyncInterval(), command.isAutoSync());
        SkillConfig saved = repository.saveOrUpdate(existing);
        return toOutput(saved);
    }

    /**
     * 添加技能路径。
     */
    @Transactional
    public SkillConfigOutput addSkillPath(String configId, String path) {
        SkillConfig config = repository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Skill config not found: " + configId));
        config.addSkillPath(path);
        SkillConfig saved = repository.saveOrUpdate(config);
        return toOutput(saved);
    }

    /**
     * 移除技能路径。
     */
    @Transactional
    public SkillConfigOutput removeSkillPath(String configId, String path) {
        SkillConfig config = repository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Skill config not found: " + configId));
        config.removeSkillPath(path);
        SkillConfig saved = repository.saveOrUpdate(config);
        return toOutput(saved);
    }

    /**
     * 删除配置。
     */
    @Transactional
    public void delete(String configId) {
        repository.deleteById(configId);
    }

    /**
     * 转换为输出 DTO。
     */
    private SkillConfigOutput toOutput(SkillConfig config) {
        SkillConfigOutput output = new SkillConfigOutput();
        output.setId(config.getId());
        output.setTenantId(config.getTenantId());
        output.setWorkspaceId(config.getWorkspaceId());
        output.setName(config.getName());
        output.setDescription(config.getDescription());
        output.setSkillPaths(config.getSkillPaths());
        output.setSyncEnabled(config.isSyncEnabled());
        output.setSyncInterval(config.getSyncInterval());
        output.setAutoSync(config.isAutoSync());
        output.setEnabled(config.isEnabled());
        output.setCreatedAt(config.getCreatedAt());
        output.setUpdatedAt(config.getUpdatedAt());
        return output;
    }
}
