package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.domain.model.Agent;
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

    private AgentEntity toEntity(Agent agent) {
        AgentEntity entity = new AgentEntity();
        entity.setId(agent.getId());
        entity.setTenantId(agent.getTenantId());
        entity.setWorkspaceId(agent.getWorkspaceId());
        entity.setAgentCode(agent.getAgentCode());
        entity.setName(agent.getName());
        entity.setDescription(agent.getDescription());
        entity.setStatus(agent.getStatus());
        entity.setEnabled(agent.isEnabled());
        entity.setCreatedAt(agent.getCreatedAt());
        entity.setUpdatedAt(agent.getUpdatedAt());
        entity.setCreatedBy(agent.getCreatedBy());
        entity.setUpdatedBy(agent.getUpdatedBy());
        return entity;
    }

    private Agent toDomain(AgentEntity entity) {
        Agent agent = new Agent();
        agent.setId(entity.getId());
        agent.setTenantId(entity.getTenantId());
        agent.setWorkspaceId(entity.getWorkspaceId());
        agent.setAgentCode(entity.getAgentCode());
        agent.setName(entity.getName());
        agent.setDescription(entity.getDescription());
        agent.setStatus(entity.getStatus());
        agent.setEnabled(entity.isEnabled());
        agent.setCreatedAt(entity.getCreatedAt());
        agent.setUpdatedAt(entity.getUpdatedAt());
        agent.setCreatedBy(entity.getCreatedBy());
        agent.setUpdatedBy(entity.getUpdatedBy());
        return agent;
    }
}
