package com.agenthub.infrastructure.store.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.GuardrailStrategyRepository;
import com.agenthub.domain.model.GuardrailStrategy;
import com.agenthub.infrastructure.store.db.entity.GuardrailStrategyEntity;
import com.agenthub.infrastructure.store.db.mapper.GuardrailStrategyMybatisMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisGuardrailStrategyRepository implements GuardrailStrategyRepository {
    private final GuardrailStrategyMybatisMapper mapper;

    public MybatisGuardrailStrategyRepository(GuardrailStrategyMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GuardrailStrategy save(GuardrailStrategy strategy) {
        GuardrailStrategyEntity entity = toEntity(strategy);
        mapper.insertOrUpdate(entity);
        return strategy;
    }

    @Override
    public Optional<GuardrailStrategy> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<GuardrailStrategy> findByWorkspace(String workspaceId) {
        LambdaQueryWrapper<GuardrailStrategyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuardrailStrategyEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private GuardrailStrategyEntity toEntity(GuardrailStrategy strategy) {
        GuardrailStrategyEntity entity = new GuardrailStrategyEntity();
        entity.setId(strategy.getId());
        entity.setTenantId(strategy.getTenantId());
        entity.setWorkspaceId(strategy.getWorkspaceId());
        entity.setName(strategy.getName());
        entity.setDescription(strategy.getDescription());
        entity.setInputValidationEnabled(strategy.isInputValidationEnabled());
        entity.setOutputValidationEnabled(strategy.isOutputValidationEnabled());
        entity.setPiiDetectionEnabled(strategy.isPiiDetectionEnabled());
        entity.setPiiMaskingEnabled(strategy.isPiiMaskingEnabled());
        entity.setPromptInjectionDetection(strategy.isPromptInjectionDetection());
        entity.setMaxInputLength(strategy.getMaxInputLength());
        entity.setMaxOutputLength(strategy.getMaxOutputLength());
        entity.setCreatedAt(strategy.getCreatedAt());
        entity.setUpdatedAt(strategy.getUpdatedAt());
        return entity;
    }

    private GuardrailStrategy toDomain(GuardrailStrategyEntity entity) {
        return GuardrailStrategy.rebuild(
            entity.getId(),
            entity.getWorkspaceId(),
            entity.getName(),
            entity.getDescription(),
            Boolean.TRUE.equals(entity.getInputValidationEnabled()),
            Boolean.TRUE.equals(entity.getOutputValidationEnabled()),
            Boolean.TRUE.equals(entity.getPiiDetectionEnabled()),
            Boolean.TRUE.equals(entity.getPiiMaskingEnabled()),
            Boolean.TRUE.equals(entity.getPromptInjectionDetection()),
            entity.getMaxInputLength() != null ? entity.getMaxInputLength() : 10000,
            entity.getMaxOutputLength() != null ? entity.getMaxOutputLength() : 4000,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void applyValidationSettings(GuardrailStrategy strategy, GuardrailStrategyEntity entity) {
        if (Boolean.TRUE.equals(entity.getInputValidationEnabled())) {
            strategy.enableInputValidation();
        }
        if (Boolean.TRUE.equals(entity.getOutputValidationEnabled())) {
            strategy.enableOutputValidation();
        }
        if (Boolean.TRUE.equals(entity.getPromptInjectionDetection())) {
            strategy.enablePromptInjectionDetection();
        }
    }

    private void applyPiiSettings(GuardrailStrategy strategy, GuardrailStrategyEntity entity) {
        if (Boolean.TRUE.equals(entity.getPiiDetectionEnabled())) {
            strategy.enablePiiDetection(Boolean.TRUE.equals(entity.getPiiMaskingEnabled()));
        }
    }

    private void applyLengthLimits(GuardrailStrategy strategy, GuardrailStrategyEntity entity) {
        if (entity.getMaxInputLength() != null && entity.getMaxOutputLength() != null) {
            strategy.setLengthLimits(entity.getMaxInputLength(), entity.getMaxOutputLength());
        }
    }
}
