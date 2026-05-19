package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.AgentTeamRepository;
import com.agenthub.domain.model.agent.AgentTeam;
import com.agenthub.infrastructure.store.db.entity.AgentTeamEntity;
import com.agenthub.infrastructure.store.db.mapper.AgentTeamMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisAgentTeamRepository implements AgentTeamRepository {
    private final AgentTeamMybatisMapper mapper;

    public MybatisAgentTeamRepository(AgentTeamMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AgentTeam save(AgentTeam team) {
        AgentTeamEntity entity = toEntity(team);
        mapper.insertOrUpdate(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<AgentTeam> findById(String teamId) {
        return Optional.ofNullable(mapper.selectById(teamId)).map(this::toDomain);
    }

    @Override
    public List<AgentTeam> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AgentTeam> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        LambdaQueryWrapper<AgentTeamEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentTeamEntity::getTenantId, tenantId)
                .eq(AgentTeamEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String teamId) {
        mapper.deleteById(teamId);
    }

    private AgentTeamEntity toEntity(AgentTeam team) {
        AgentTeamEntity entity = new AgentTeamEntity();
        entity.setId(team.getId());
        entity.setTenantId(team.getTenantId());
        entity.setWorkspaceId(team.getWorkspaceId());
        entity.setTeamCode(team.getTeamCode());
        entity.setName(team.getName());
        entity.setDescription(team.getDescription());
        entity.setCoordinationMode(team.getCoordinationMode());
        entity.setMemberConfig(team.getMemberConfig());
        entity.setStatus(team.getStatus());
        entity.setCreatedAt(team.getCreatedAt());
        entity.setUpdatedAt(team.getUpdatedAt());
        return entity;
    }

    private AgentTeam toDomain(AgentTeamEntity entity) {
        AgentTeam team = new AgentTeam();
        team.setId(entity.getId());
        team.setTenantId(entity.getTenantId());
        team.setWorkspaceId(entity.getWorkspaceId());
        team.setTeamCode(entity.getTeamCode());
        team.setName(entity.getName());
        team.setDescription(entity.getDescription());
        team.setCoordinationMode(entity.getCoordinationMode());
        team.setMemberConfig(entity.getMemberConfig());
        team.setStatus(entity.getStatus());
        team.setCreatedAt(entity.getCreatedAt());
        team.setUpdatedAt(entity.getUpdatedAt());
        return team;
    }
}
