package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.domain.enums.AgentRuntimeCategory;
import com.agenthub.domain.enums.AgentType;
import com.agenthub.domain.model.agent.Agent;
import com.agenthub.infrastructure.store.db.entity.AgentEntity;
import com.agenthub.infrastructure.store.db.mapper.AgentMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisAgentRepository implements AgentRepository {
    private final AgentMybatisMapper mapper;

    public MybatisAgentRepository(AgentMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Agent save(Agent agent) {
        AgentEntity entity = toEntity(agent);
        if (entity.getId() == null && findByName(agent.getName()) != null) {
            throw new RuntimeException("Agent name already exists");
        }
        mapper.insertOrUpdate(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Agent> findById(String agentId) {
        return Optional.ofNullable(mapper.selectById(agentId)).map(this::toDomain);
    }

    @Override
    public List<Agent> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String agentId) {
        mapper.deleteById(agentId);
    }

    @Override
    public List<Agent> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentEntity::getTenantId, tenantId)
                .eq(AgentEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public Agent findByName(String name) {
        LambdaQueryWrapper<AgentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentEntity::getName, name);
        AgentEntity agentEntity = mapper.selectOne(queryWrapper);
        return toDomain(agentEntity);
    }

    private AgentEntity toEntity(Agent agent) {
        if (agent == null) return null;
        AgentEntity entity = BeanUtil.copyProperties(agent, AgentEntity.class);
        entity.setType(agent.getType() != null ? agent.getType().name() : null);
        entity.setRuntimeCategory(agent.getRuntimeCategory() != null ? agent.getRuntimeCategory().name() : null);
        return entity;
    }

    private Agent toDomain(AgentEntity entity) {
        if (entity == null) return null;
        Agent agent = BeanUtil.copyProperties(entity, Agent.class);
        agent.setType(entity.getType() != null ? AgentType.valueOf(entity.getType()) : null);
        agent.setRuntimeCategory(entity.getRuntimeCategory() != null ? AgentRuntimeCategory.valueOf(entity.getRuntimeCategory()) : null);
        return agent;
    }
}
