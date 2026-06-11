package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.GuardrailStrategyRepository;
import com.agenthub.domain.model.strategy.GuardrailStrategy;
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
        return toDomain(entity);
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
        return BeanUtil.copyProperties(strategy, GuardrailStrategyEntity.class);
    }

    private GuardrailStrategy toDomain(GuardrailStrategyEntity entity) {
        GuardrailStrategy.State state = new GuardrailStrategy.State();
        BeanUtil.copyProperties(entity, state);
        return GuardrailStrategy.rebuild(state);
    }
}
