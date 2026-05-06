package com.agenthub.infrastructure.store.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.ToolStrategyRepository;
import com.agenthub.domain.model.ToolStrategy;
import com.agenthub.infrastructure.store.db.entity.ToolStrategyEntity;
import com.agenthub.infrastructure.store.db.mapper.ToolStrategyMybatisMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisToolStrategyRepository implements ToolStrategyRepository {
    private final ToolStrategyMybatisMapper mapper;

    public MybatisToolStrategyRepository(ToolStrategyMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ToolStrategy save(ToolStrategy strategy) {
        ToolStrategyEntity entity = toEntity(strategy);
        mapper.insertOrUpdate(entity);
        return strategy;
    }

    @Override
    public Optional<ToolStrategy> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<ToolStrategy> findByWorkspace(String workspaceId) {
        LambdaQueryWrapper<ToolStrategyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolStrategyEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private ToolStrategyEntity toEntity(ToolStrategy strategy) {
        ToolStrategyEntity entity = new ToolStrategyEntity();
        entity.setId(strategy.getId());
        entity.setTenantId(strategy.getTenantId());
        entity.setWorkspaceId(strategy.getWorkspaceId());
        entity.setName(strategy.getName());
        entity.setDescription(strategy.getDescription());
        entity.setMaxConcurrentCalls(strategy.getMaxConcurrentCalls());
        entity.setTimeoutSeconds(strategy.getTimeoutSeconds());
        entity.setRetryCount(strategy.getRetryCount());
        entity.setFallbackEnabled(strategy.isFallbackEnabled());
        entity.setCreatedAt(strategy.getCreatedAt());
        entity.setUpdatedAt(strategy.getUpdatedAt());
        return entity;
    }

    private ToolStrategy toDomain(ToolStrategyEntity entity) {
        return ToolStrategy.rebuild(
            entity.getId(),
            entity.getWorkspaceId(),
            entity.getName(),
            entity.getDescription(),
            entity.getMaxConcurrentCalls(),
            entity.getTimeoutSeconds(),
            entity.getRetryCount(),
            Boolean.TRUE.equals(entity.getFallbackEnabled()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
