package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.model.skill.Skill;
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
        queryWrapper.eq(SkillEntity::getSkillCode, skill.getSkillCode());
        SkillEntity existing = mapper.selectOne(queryWrapper);
        if (existing != null) {
            entity.setId(existing.getId());
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

    @Override
    public void updateFileStats(String skillId, int fileCount, long totalSize) {
        SkillEntity entity = mapper.selectById(skillId);
        if (entity != null) {
            entity.setFileCount(fileCount);
            entity.setTotalSize(totalSize);
            mapper.updateById(entity);
        }
    }

    @Override
    public void updateSyncTime(String skillId) {
        SkillEntity entity = mapper.selectById(skillId);
        if (entity != null) {
            entity.setLastSyncAt(Instant.now());
            mapper.updateById(entity);
        }
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
        entity.setSource(skill.getSource());
        entity.setSourcePath(skill.getSourcePath());
        entity.setZipStoragePath(skill.getZipStoragePath());
        entity.setConfigId(skill.getConfigId());
        entity.setFileCount(skill.getFileCount());
        entity.setTotalSize(skill.getTotalSize());
        entity.setEnabled(skill.isEnabled());
        entity.setCreatedAt(skill.getCreatedAt());
        entity.setUpdatedAt(skill.getUpdatedAt());
        entity.setLastSyncAt(skill.getLastSyncAt());
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
        skill.setSource(entity.getSource());
        skill.setSourcePath(entity.getSourcePath());
        skill.setZipStoragePath(entity.getZipStoragePath());
        skill.setConfigId(entity.getConfigId());
        skill.setFileCount(entity.getFileCount());
        skill.setTotalSize(entity.getTotalSize());
        skill.setEnabled(entity.isEnabled());
        skill.setCreatedAt(entity.getCreatedAt());
        skill.setUpdatedAt(entity.getUpdatedAt());
        skill.setLastSyncAt(entity.getLastSyncAt());
        return skill;
    }
}
