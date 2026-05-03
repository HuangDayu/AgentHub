package com.agenthub.infrastructure.persistence.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.infrastructure.persistence.db.entity.AgentConfigEntity;
import com.agenthub.infrastructure.persistence.db.mapper.AgentConfigMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MybatisAgentConfigRepository implements AgentConfigRepository {
    private final AgentConfigMybatisMapper mapper;

    public MybatisAgentConfigRepository(AgentConfigMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AgentConfig save(AgentConfig config) {
        AgentConfigEntity entity = toEntity(config);
        if (exists(entity)) {
            throw new IllegalArgumentException("AgentConfig already exists.");
        }
        mapper.insert(entity);
        return toDomain(entity);
    }

    private boolean exists(AgentConfigEntity entity) {
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentConfigEntity::getAgentId, entity.getAgentId());
        queryWrapper.eq(AgentConfigEntity::getCategory, entity.getCategory());
        queryWrapper.eq(AgentConfigEntity::getType, entity.getType());
        queryWrapper.eq(AgentConfigEntity::getConfigId, entity.getConfigId());
        return mapper.selectOne(queryWrapper) != null;
    }

    @Override
    public Optional<AgentConfig> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<AgentConfig> findByAgentId(String agentId) {
        return mapper.selectByAgentId(agentId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AgentConfig> findByAgentIdAndCategory(String agentId, AgentConfig.Category category) {
        return mapper.selectByAgentIdAndCategory(agentId, category.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AgentConfig> findEnabledAgentConfigs(String agentId, AgentConfig.Category category, AgentConfig.Type type) {
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentConfigEntity::getAgentId, agentId);
        queryWrapper.eq(AgentConfigEntity::getCategory, category.name());
        queryWrapper.eq(AgentConfigEntity::getType, type.name());
        queryWrapper.eq(AgentConfigEntity::isEnabled, true);
        return mapper.selectList(queryWrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public AgentConfig findOneAgentConfig(String agentId, AgentConfig.Category category, AgentConfig.Type type) {
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentConfigEntity::getAgentId, agentId);
        queryWrapper.eq(AgentConfigEntity::getCategory, category.name());
        queryWrapper.eq(AgentConfigEntity::getType, type.name());
        return toDomain(mapper.selectOne(queryWrapper));
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public void deleteByAgentId(String agentId) {
        mapper.delete(new QueryWrapper<AgentConfigEntity>().eq("agent_id", agentId));
    }

    @Override
    public AgentConfig update(AgentConfig config) {
        AgentConfigEntity entity = toEntity(config);
        mapper.updateById(entity);
        return toDomain(entity);
    }

    @Override
    public String getConfigId(String agentId, AgentConfig.Category category, AgentConfig.Type type) {
        AgentConfig config = findOneAgentConfig(agentId, category, type);
        if (config == null) {
            throw new IllegalStateException("未找到对应的配置");
        }
        return config.configId();
    }

    private AgentConfigEntity toEntity(AgentConfig config) {
        AgentConfigEntity entity = new AgentConfigEntity();
        entity.setId(config.id());
        entity.setAgentId(config.agentId());
        entity.setCategory(config.category().name());
        entity.setType(config.type().name());
        entity.setConfigId(config.configId());
        entity.setDescription(config.description());
        entity.setPriority(config.priority());
        entity.setEnabled(config.enabled());
        entity.setCreatedAt(config.createdAt());
        entity.setUpdatedAt(config.updatedAt());
        return entity;
    }

    private AgentConfig toDomain(AgentConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AgentConfig(
                entity.getId(), entity.getAgentId(),
                AgentConfig.Category.valueOf(entity.getCategory()),
                AgentConfig.Type.valueOf(entity.getType()),
                entity.getConfigId(), entity.getDescription(),
                entity.getPriority() != null ? entity.getPriority() : 0,
                entity.isEnabled(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
