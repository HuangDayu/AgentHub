package com.agenthub.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.ModelStrategyRepository;
import com.agenthub.domain.model.ModelStrategy;
import com.agenthub.infrastructure.persistence.entity.ModelStrategyEntity;
import com.agenthub.infrastructure.persistence.mapper.ModelStrategyMybatisMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisModelStrategyRepository implements ModelStrategyRepository {
    private final ModelStrategyMybatisMapper mapper;

    public MybatisModelStrategyRepository(ModelStrategyMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ModelStrategy save(ModelStrategy strategy) {
        ModelStrategyEntity entity = toEntity(strategy);
        mapper.insertOrUpdate(entity);
        return strategy;
    }

    @Override
    public Optional<ModelStrategy> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<ModelStrategy> findByWorkspace(String workspaceId) {
        LambdaQueryWrapper<ModelStrategyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelStrategyEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private ModelStrategyEntity toEntity(ModelStrategy strategy) {
        ModelStrategyEntity entity = new ModelStrategyEntity();
        entity.setId(strategy.getId());
        entity.setTenantId(strategy.getTenantId());
        entity.setWorkspaceId(strategy.getWorkspaceId());
        entity.setName(strategy.getName());
        entity.setDescription(strategy.getDescription());
        entity.setTemperature(strategy.getTemperature());
        entity.setMaxTokens(strategy.getMaxTokens());
        entity.setTopP(strategy.getTopP());
        entity.setFrequencyPenalty(strategy.getFrequencyPenalty());
        entity.setPresencePenalty(strategy.getPresencePenalty());
        entity.setCreatedAt(strategy.getCreatedAt());
        entity.setUpdatedAt(strategy.getUpdatedAt());
        return entity;
    }

    private ModelStrategy toDomain(ModelStrategyEntity entity) {
        return ModelStrategy.rebuild(
            entity.getId(),
            entity.getWorkspaceId(),
            entity.getName(),
            entity.getDescription(),
            entity.getTemperature(),
            entity.getMaxTokens(),
            entity.getTopP(),
            entity.getFrequencyPenalty(),
            entity.getPresencePenalty(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
