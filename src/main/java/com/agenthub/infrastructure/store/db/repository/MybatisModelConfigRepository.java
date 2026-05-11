package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.ModelConfigRepository;
import com.agenthub.domain.model.ModelConfig;
import com.agenthub.infrastructure.store.db.entity.ModelConfigEntity;
import com.agenthub.infrastructure.store.db.mapper.ModelConfigMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模型配置 MyBatis Plus 仓储实现。
 */
@Repository
public class MybatisModelConfigRepository implements ModelConfigRepository {

    private static final Function<ModelConfigEntity, ModelConfig> PO_TO_DOMAIN = po ->
            BeanUtil.copyProperties(po, ModelConfig.class);

    private static final Function<ModelConfig, ModelConfigEntity> DOMAIN_TO_PO = config ->
            BeanUtil.copyProperties(config, ModelConfigEntity.class);

    private final ModelConfigMybatisMapper mapper;

    public MybatisModelConfigRepository(ModelConfigMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ModelConfig save(ModelConfig config) {
        ModelConfigEntity modelConfigEntity = DOMAIN_TO_PO.apply(config);
        if (modelConfigEntity.getId() == null) {
            mapper.insert(modelConfigEntity);
        } else {
            mapper.updateById(modelConfigEntity);
        }
        return PO_TO_DOMAIN.apply(modelConfigEntity);
    }

    @Override
    public Optional<ModelConfig> findById(String id) {
        ModelConfigEntity modelConfigEntity = mapper.selectById(id);
        return modelConfigEntity != null ? Optional.of(PO_TO_DOMAIN.apply(modelConfigEntity)) : Optional.empty();
    }

    @Override
    public Optional<ModelConfig> findByIdAndTenant(String id, String tenantId) {
        ModelConfigEntity modelConfigEntity = mapper.selectByIdAndTenant(id, tenantId);
        return modelConfigEntity != null ? Optional.of(PO_TO_DOMAIN.apply(modelConfigEntity)) : Optional.empty();
    }

    @Override
    public List<ModelConfig> findByTenant(String tenantId) {
        return mapper.selectByTenant(tenantId).stream()
                .map(PO_TO_DOMAIN)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelConfig> findByTenantAndType(String tenantId, String type) {
        return mapper.selectByTenantAndType(tenantId, type).stream()
                .map(PO_TO_DOMAIN)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelConfig> getByType(String type) {
        return mapper.selectByType(type).stream()
                .map(PO_TO_DOMAIN)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelConfig> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream()
                .map(PO_TO_DOMAIN)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelConfig> findEnabledAll(Boolean enabled) {
        return mapper.selectByEnabled(enabled).stream()
                .map(PO_TO_DOMAIN)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelConfig> findByWorkspace(String workspaceId, String type) {
        LambdaQueryWrapper<ModelConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelConfigEntity::getWorkspaceId, workspaceId);
        queryWrapper.eq(ModelConfigEntity::getType, type);
        return mapper.selectList(queryWrapper).stream()
                .map(PO_TO_DOMAIN)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelConfig> findByTenantAndEnabled(String tenantId, Boolean enabled) {
        return mapper.selectByTenantAndEnabled(tenantId, enabled).stream()
                .map(PO_TO_DOMAIN)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByIdAndTenant(String id, String tenantId) {
        return mapper.deleteByIdAndTenant(id, tenantId) > 0;
    }
}