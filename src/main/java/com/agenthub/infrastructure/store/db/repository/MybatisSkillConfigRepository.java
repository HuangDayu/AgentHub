package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.SkillConfigRepository;
import com.agenthub.domain.model.skill.SkillConfig;
import com.agenthub.infrastructure.store.db.entity.SkillConfigEntity;
import com.agenthub.infrastructure.store.db.mapper.SkillConfigMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 技能配置仓储实现。
 */
@Component
@RequiredArgsConstructor
public class MybatisSkillConfigRepository implements SkillConfigRepository {

    private final SkillConfigMybatisMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public SkillConfig saveOrUpdate(SkillConfig config) {
        SkillConfigEntity entity = toEntity(config);
        SkillConfigEntity existing = mapper.selectById(entity.getId());
        if (existing != null) {
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<SkillConfig> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<SkillConfig> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId) {
        return mapper.selectList(
                new LambdaQueryWrapper<SkillConfigEntity>()
                        .eq(SkillConfigEntity::getTenantId, tenantId)
                        .eq(SkillConfigEntity::getWorkspaceId, workspaceId))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<SkillConfig> findAll() {
        return mapper.selectList(null).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    /**
     * 转换为实体。
     */
    private SkillConfigEntity toEntity(SkillConfig domain) {
        SkillConfigEntity entity = new SkillConfigEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setWorkspaceId(domain.getWorkspaceId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setSkillPaths(toJson(domain.getSkillPaths()));
        entity.setSyncEnabled(domain.isSyncEnabled());
        entity.setSyncInterval(domain.getSyncInterval());
        entity.setAutoSync(domain.isAutoSync());
        entity.setEnabled(domain.isEnabled());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    /**
     * 转换为领域模型。
     */
    private SkillConfig toDomain(SkillConfigEntity entity) {
        SkillConfig domain = new SkillConfig();
        domain.setId(entity.getId());
        domain.setTenantId(entity.getTenantId());
        domain.setWorkspaceId(entity.getWorkspaceId());
        domain.setName(entity.getName());
        domain.setDescription(entity.getDescription());
        domain.setSkillPaths(fromJson(entity.getSkillPaths()));
        domain.setSyncEnabled(entity.getSyncEnabled() != null ? entity.getSyncEnabled() : true);
        domain.setSyncInterval(entity.getSyncInterval() != null ? entity.getSyncInterval() : 3600);
        domain.setAutoSync(entity.getAutoSync() != null ? entity.getAutoSync() : false);
        domain.setEnabled(entity.getEnabled() != null ? entity.getEnabled() : true);
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    /**
     * JSON 序列化。
     */
    private String toJson(List<String> paths) {
        try {
            return objectMapper.writeValueAsString(paths);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * JSON 反序列化。
     */
    private List<String> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
