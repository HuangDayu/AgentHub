package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
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
        SkillEntity existing = mapper.selectOne(queryWrapper, false);
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

    @Override
    public void updateById(Skill skill) {
        mapper.updateById(toEntity(skill));
    }

    @Override
    public List<Skill> search(String keyword) {
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(SkillEntity::getName, keyword)
                .or().like(SkillEntity::getSkillCode, keyword)
                .or().like(SkillEntity::getDescription, keyword));
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Skill> findBySkillCode(String skillCode) {
        LambdaQueryWrapper<SkillEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkillEntity::getSkillCode, skillCode)
                .or().eq(SkillEntity::getName, skillCode);
        SkillEntity skillEntity = mapper.selectOne(queryWrapper);
        return Optional.ofNullable(toDomain(skillEntity));
    }

    private SkillEntity toEntity(Skill skill) {
        if (skill == null) {
            return null;
        }
        return BeanUtil.copyProperties(skill, SkillEntity.class);
    }

    private Skill toDomain(SkillEntity entity) {
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, Skill.class);
    }
}
