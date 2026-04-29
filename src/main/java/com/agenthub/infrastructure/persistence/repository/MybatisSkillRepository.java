package com.agenthub.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.model.Skill;
import com.agenthub.infrastructure.persistence.entity.SkillEntity;
import com.agenthub.infrastructure.persistence.mapper.SkillMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisSkillRepository implements SkillRepository {
    private final SkillMybatisMapper mapper;

    public MybatisSkillRepository(SkillMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Skill save(Skill skill) {
        SkillEntity entity = toEntity(skill);
        mapper.insertOrUpdate(entity);
        return skill;
    }

    @Override
    public Optional<Skill> findById(String skillId) {
        return Optional.ofNullable(mapper.selectById(skillId)).map(this::toDomain);
    }

    @Override
    public List<Skill> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Skill> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillEntity::getTenantId, tenantId)
               .eq(SkillEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String skillId) {
        mapper.deleteById(skillId);
    }

    private SkillEntity toEntity(Skill skill) {
        SkillEntity entity = new SkillEntity();
        entity.setId(skill.getId());
        entity.setTenantId(skill.getTenantId());
        entity.setWorkspaceId(skill.getWorkspaceId());
        entity.setSkillCode(skill.getSkillCode());
        entity.setName(skill.getName());
        entity.setDescription(skill.getDescription());
        entity.setSkillType(skill.getSkillType());
        entity.setDefinition(skill.getDefinition());
        entity.setParameters(skill.getParameters());
        entity.setEnabled(skill.isEnabled());
        entity.setCreatedAt(skill.getCreatedAt());
        entity.setUpdatedAt(skill.getUpdatedAt());
        return entity;
    }

    private Skill toDomain(SkillEntity entity) {
        Skill skill = new Skill();
        skill.setId(entity.getId());
        skill.setTenantId(entity.getTenantId());
        skill.setWorkspaceId(entity.getWorkspaceId());
        skill.setSkillCode(entity.getSkillCode());
        skill.setName(entity.getName());
        skill.setDescription(entity.getDescription());
        skill.setSkillType(entity.getSkillType());
        skill.setDefinition(entity.getDefinition());
        skill.setParameters(entity.getParameters());
        skill.setEnabled(entity.isEnabled());
        skill.setCreatedAt(entity.getCreatedAt());
        skill.setUpdatedAt(entity.getUpdatedAt());
        return skill;
    }
}
