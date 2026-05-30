package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.model.tools.Skill;
import com.agenthub.infrastructure.store.db.entity.SkillEntity;
import com.agenthub.infrastructure.store.db.mapper.SkillMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
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
    public Skill saveOrUpdate(Skill skill) {
        SkillEntity entity = toEntity(skill);
        LambdaQueryWrapper<SkillEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkillEntity::getName, entity.getName());
        queryWrapper.eq(SkillEntity::getSkillPath, entity.getSkillPath());
        SkillEntity skillEntity = mapper.selectOne(queryWrapper);
        if (skillEntity != null) {
            entity.setId(skillEntity.getId());
        }
        mapper.insertOrUpdate(entity);
        return toDomain(entity);
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

    @Override
    public List<Skill> findByWorkspaceId(String workspaceId) {
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Skill> findByIds(List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(toolIds).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteBefore(Instant minus) {
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(SkillEntity::getUpdatedAt, minus);
        mapper.delete(wrapper);
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
        entity.setSkillFilesTree(skill.getSkillFilesTree());
        entity.setSkillPath(skill.getSkillPath());
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
        skill.setSkillFilesTree(entity.getSkillFilesTree());
        skill.setSkillPath(entity.getSkillPath());
        skill.setEnabled(entity.isEnabled());
        skill.setCreatedAt(entity.getCreatedAt());
        skill.setUpdatedAt(entity.getUpdatedAt());
        return skill;
    }
}
