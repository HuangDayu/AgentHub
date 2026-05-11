package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.common.exception.ConflictException;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.command.CreateVectorStoreConfigCommand;
import com.agenthub.application.command.UpdateVectorStoreConfigCommand;
import com.agenthub.application.dto.VectorStoreTestOutput;
import com.agenthub.application.port.out.VectorPoolManagerPort;
import com.agenthub.application.port.out.repositories.VectorStoreConfigRepository;
import com.agenthub.domain.model.VectorStoreConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 向量库配置应用服务。
 * <p>
 * 提供向量库配置的 CRUD 操作，负责协调领域层和基础设施层。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class VectorStoreConfigUseCase {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfigUseCase.class);

    private final VectorStoreConfigRepository repository;
    private final VectorPoolManagerPort vectorPoolManagerPort;

    /**
     * 创建新的向量库配置。
     *
     * @param command 创建命令
     * @return 创建的向量库配置
     */
    @Transactional
    public VectorStoreConfig create(CreateVectorStoreConfigCommand command) {
        validateNameNotExists(command.getName());
        VectorStoreConfig config = BeanUtil.copyProperties(command, VectorStoreConfig.class);
        return repository.save(config);
    }

    /**
     * 验证名称不存在。
     */
    private void validateNameNotExists(String name) {
        repository.findByName(name)
                .ifPresent(existing -> {
                    throw new ConflictException("Vector store config with name '" + name);
                });
    }


    /**
     * 根据 ID 获取向量库配置。
     *
     * @param id 配置 ID
     * @return 向量库配置
     * @throws IllegalArgumentException 如果配置不存在
     */
    public VectorStoreConfig getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vector store config not found: " + id));
    }

    /**
     * 获取指定租户的所有向量库配置。
     *
     * @param tenantId 租户 ID
     * @return 配置列表
     */
    public List<VectorStoreConfig> listByTenant(String tenantId) {
        return repository.findAllByTenantId(tenantId);
    }

    /**
     * 更新向量库配置。
     *
     * @param command 更新命令
     * @return 更新后的配置
     */
    @Transactional
    public VectorStoreConfig update(UpdateVectorStoreConfigCommand command) {
        VectorStoreConfig updated = BeanUtil.copyProperties(command, VectorStoreConfig.class);
        return repository.save(updated);
    }

    /**
     * 查找现有配置。
     */
    private VectorStoreConfig findExistingConfig(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vector store config not found: " + id));
    }



    /**
     * 删除向量库配置。
     *
     * @param tenantId 租户 ID
     * @param id       配置 ID
     */
    @Transactional
    public void delete(String tenantId, String id) {
        VectorStoreConfig existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vector store config not found: " + id));
        repository.deleteById(id);
    }

    /**
     * 获取指定租户的所有启用的向量库配置（按更新时间倒序）。
     *
     * @param tenantId 租户 ID
     * @return 启用的配置列表
     */
    public List<VectorStoreConfig> listEnabled(String tenantId) {
        return repository.findAllByTenantId(tenantId)
                .stream()
                .filter(VectorStoreConfig::getEnabled)
                .toList();
    }

    /**
     * 查找所有配置。
     */
    public Collection<VectorStoreConfig> findAll() {
        return repository.findAll();
    }

    /**
     * 根据 ID 删除配置。
     */
    public void deleteById(String configId) {
        repository.deleteById(configId);
    }


    public VectorStoreTestOutput testConnection(String configId) {
        return vectorPoolManagerPort.testConnection(configId);
    }
}
