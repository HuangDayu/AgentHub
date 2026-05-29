package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.SubagentRepository;
import com.agenthub.domain.model.agent.Subagent;
import com.agenthub.infrastructure.store.db.entity.SubagentEntity;
import com.agenthub.infrastructure.store.db.mapper.SubagentMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 基于MyBatis的子智能体仓储实现。
 */
@Component
@Primary
public class MybatisSubagentRepository implements SubagentRepository {
    private final SubagentMybatisMapper mapper;

    public MybatisSubagentRepository(SubagentMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Subagent save(Subagent subagent) {
        SubagentEntity entity = toEntity(subagent);
        mapper.insertOrUpdate(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Subagent> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<Subagent> findByParentAgentId(String parentAgentId) {
        LambdaQueryWrapper<SubagentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubagentEntity::getParentAgentId, parentAgentId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }



    @Override
    public List<Subagent> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        LambdaQueryWrapper<SubagentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubagentEntity::getTenantId, tenantId)
                .eq(SubagentEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    private SubagentEntity toEntity(Subagent subagent) {
        if (subagent == null) return null;
        return BeanUtil.copyProperties(subagent, SubagentEntity.class);
    }

    private Subagent toDomain(SubagentEntity entity) {
        if (entity == null) return null;
        return BeanUtil.copyProperties(entity, Subagent.class);
    }
}
