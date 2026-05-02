package com.agenthub.application.usecase;

import com.agenthub.application.command.CreateModelConfigCommand;
import com.agenthub.application.command.UpdateModelConfigCommand;
import com.agenthub.application.dto.ModelTestOutput;
import com.agenthub.application.port.out.ModelPoolManagerPort;
import com.agenthub.application.port.out.repositories.ModelConfigRepository;
import com.agenthub.domain.model.ModelConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 模型配置应用服务。
 * <p>
 * 负责模型配置的 CRUD 操作，并通过 ModelFactoryRegistry 动态创建模型实例。
 * 模型实例带有缓存，避免重复创建开销。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ModelConfigUseCase {

    private static final Logger log = LoggerFactory.getLogger(ModelConfigUseCase.class);

    private final ModelConfigRepository modelConfigRepository;
    private final ModelPoolManagerPort modelPoolManagerPort;



    /**
     * 创建模型配置。
     */
    @Transactional
    public ModelConfig create(CreateModelConfigCommand command) {
        Instant now = Instant.now();
        ModelConfig config = buildNewConfig(command, now);
        ModelConfig saved = modelConfigRepository.save(config);
        logCreated(saved);
        return saved;
    }

    /**
     * 构建新的模型配置。
     */
    private ModelConfig buildNewConfig(CreateModelConfigCommand command, Instant now) {
        return new ModelConfig(
                null, command.name(), command.type(), command.supplier(),
                command.apiKey(), command.baseUrl(), command.model(),
                command.enabled(), now, now, command.createdBy()
        );
    }

    /**
     * 记录创建日志。
     */
    private void logCreated(ModelConfig saved) {
        log.info("Created model config: id={}, name={}, supplier={}, type={}",
                saved.id(), saved.name(), saved.supplier(), saved.type());
    }

    /**
     * 更新模型配置。
     */
    @Transactional
    public ModelConfig update(UpdateModelConfigCommand command) {
        ModelConfig existing = findExistingConfig(command.id());
        ModelConfig updated = buildUpdatedConfig(existing, command);
        ModelConfig saved = modelConfigRepository.save(updated);
        modelPoolManagerPort.evictCache(command.id());
        log.info("Updated model config: id={}", saved.id());
        return saved;
    }

    /**
     * 查找现有配置。
     */
    private ModelConfig findExistingConfig(String id) {
        return modelConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model config not found: id=" + id));
    }

    /**
     * 构建更新后的配置。
     */
    private ModelConfig buildUpdatedConfig(ModelConfig existing, UpdateModelConfigCommand command) {
        return new ModelConfig(
                existing.id(),
                command.name() != null ? command.name() : existing.name(),
                command.type() != null ? command.type() : existing.type(),
                command.supplier() != null ? command.supplier() : existing.supplier(),
                command.apiKey() != null ? command.apiKey() : existing.apiKey(),
                command.baseUrl() != null ? command.baseUrl() : existing.baseUrl(),
                command.model() != null ? command.model() : existing.model(),
                command.enabled() != null ? command.enabled() : existing.enabled(),
                existing.createdAt(),
                Instant.now(),
                existing.createdBy()
        );
    }

    /**
     * 根据 ID 获取模型配置。
     */
    public Optional<ModelConfig> getById(String id) {
        return modelConfigRepository.findById(id);
    }

    /**
     * 根据 ID 和租户 ID 获取（租户隔离）。
     */
    public Optional<ModelConfig> getByIdAndTenant(String id, String tenantId) {
        return modelConfigRepository.findByIdAndTenant(id, tenantId);
    }

    /**
     * 获取租户下所有模型配置。
     */
    public List<ModelConfig> getByTenant(String tenantId) {
        return modelConfigRepository.findByTenant(tenantId);
    }

    /**
     * 获取租户下指定类型的模型配置。
     */
    public List<ModelConfig> getByTenantAndType(String tenantId, String type) {
        return modelConfigRepository.findByTenantAndType(tenantId, type);
    }

    /**
     * 根据类型获取配置。
     */
    public List<ModelConfig> getByType(String type) {
        return modelConfigRepository.getByType(type);
    }

    /**
     * 获取租户下启用的模型配置。
     */
    public List<ModelConfig> getEnabledByTenant(String tenantId) {
        return modelConfigRepository.findByTenantAndEnabled(tenantId, true);
    }

    /**
     * 删除模型配置。
     */
    @Transactional
    public boolean deleteByIdAndTenant(String id, String tenantId) {
        boolean deleted = modelConfigRepository.deleteByIdAndTenant(id, tenantId);
        if (deleted) {
            modelPoolManagerPort.evictCache(id);
            log.info("Deleted model config: id={}, tenantId={}", id, tenantId);
        }
        return deleted;
    }

    /**
     * 根据ID删除。
     */
    public boolean deleteById(String id) {
        return modelConfigRepository.deleteById(id);
    }

    /**
     * 获取所有配置列表。
     */
    public List<ModelConfig> getList() {
        return modelConfigRepository.findAll();
    }

    /**
     * 获取启用的配置列表。
     */
    public List<ModelConfig> getListByType(String type) {
        return modelConfigRepository.getByType(type);
    }

    /**
     * 获取启用的配置列表。
     */
    public List<ModelConfig> getEnabledList() {
        return modelConfigRepository.findEnabledAll(true);
    }

    public ModelTestOutput testModel(String configId) {
        return modelPoolManagerPort.testModel(configId);
    }

}
