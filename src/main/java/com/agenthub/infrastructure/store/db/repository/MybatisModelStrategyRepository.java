package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.ModelStrategyRepository;
import com.agenthub.domain.model.strategy.ModelStrategy;
import com.agenthub.infrastructure.store.db.entity.ModelStrategyEntity;
import com.agenthub.infrastructure.store.db.mapper.ModelStrategyMybatisMapper;
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
        return toDomain(entity);
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
        return BeanUtil.copyProperties(strategy, ModelStrategyEntity.class);
    }

    private ModelStrategy toDomain(ModelStrategyEntity entity) {
        ModelStrategy.State state = new ModelStrategy.State();
        BeanUtil.copyProperties(entity, state);
        return ModelStrategy.rebuild(state);
    }
}
