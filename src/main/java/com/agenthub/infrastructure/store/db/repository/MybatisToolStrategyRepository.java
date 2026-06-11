package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.ToolStrategyRepository;
import com.agenthub.domain.model.strategy.ToolStrategy;
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
    public ToolStrategy saveOrUpdate(ToolStrategy strategy) {
        ToolStrategyEntity entity = toEntity(strategy);
        mapper.insertOrUpdate(entity);
        return toDomain(entity);
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
        return BeanUtil.copyProperties(strategy, ToolStrategyEntity.class);
    }

    private ToolStrategy toDomain(ToolStrategyEntity entity) {
        ToolStrategy.State state = new ToolStrategy.State();
        BeanUtil.copyProperties(entity, state);
        return ToolStrategy.rebuild(state);
    }
}
