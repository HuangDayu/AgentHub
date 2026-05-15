package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.AgentConfigCategory;
import com.agenthub.domain.model.AgentConfigType;
import com.agenthub.infrastructure.store.db.entity.AgentConfigEntity;
import com.agenthub.infrastructure.store.db.mapper.AgentConfigMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public AgentConfig saveOrUpdate(AgentConfig config) {
        AgentConfigEntity newEntity = toEntity(config);
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = buildQueryWrapper(config.getCategory().getSum(), newEntity);
        AgentConfigEntity configEntity = mapper.selectOne(queryWrapper);
        if (configEntity != null) {
            newEntity.setId(configEntity.getId());
        }
        mapper.insertOrUpdate(newEntity);
        return toDomain(newEntity);
    }

    private LambdaQueryWrapper<AgentConfigEntity> buildQueryWrapper(int sum, AgentConfigEntity entity) {
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentConfigEntity::getAgentId, entity.getAgentId());
        queryWrapper.eq(AgentConfigEntity::getCategory, entity.getCategory());
        queryWrapper.eq(AgentConfigEntity::getType, entity.getType());
        if (sum == 1) {
            return queryWrapper;
        }
        queryWrapper.eq(AgentConfigEntity::getConfigId, entity.getConfigId());
        return queryWrapper;
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
    public List<AgentConfig> findByAgentIdAndEnabled(String agentId) {
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentConfigEntity::getAgentId, agentId);
        queryWrapper.eq(AgentConfigEntity::isEnabled, true);
        return mapper.selectList(queryWrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AgentConfig> findByAgentIdAndCategory(String agentId, AgentConfigCategory category) {
        return mapper.selectByAgentIdAndCategory(agentId, category.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AgentConfig> findEnabledAgentConfigs(String agentId, AgentConfigCategory category, AgentConfigType type) {
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentConfigEntity::getAgentId, agentId);
        queryWrapper.eq(AgentConfigEntity::getCategory, category.name());
        queryWrapper.eq(AgentConfigEntity::getType, type.name());
        queryWrapper.eq(AgentConfigEntity::isEnabled, true);
        return mapper.selectList(queryWrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public AgentConfig findOneAgentConfig(String agentId, AgentConfigCategory category, AgentConfigType type) {
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentConfigEntity::getAgentId, agentId);
        queryWrapper.eq(AgentConfigEntity::getCategory, category.name());
        queryWrapper.eq(AgentConfigEntity::getType, type.name());
        return toDomain(mapper.selectOne(queryWrapper));
    }

    @Override
    public List<AgentConfig> findAgentConfigs(AgentConfigCategory category, AgentConfigType type, List<String> configIds) {
        LambdaQueryWrapper<AgentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AgentConfigEntity::getConfigId, configIds);
        queryWrapper.eq(AgentConfigEntity::getCategory, category.name());
        if (!AgentConfigType.ALL_TYPE.equals(type)) {
            queryWrapper.eq(AgentConfigEntity::getType, type.name());
        }
        List<AgentConfigEntity> entities = mapper.selectList(queryWrapper);
        return entities.stream().map(this::toDomain).toList();
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
    public String getConfigId(String agentId, AgentConfigCategory category, AgentConfigType type) {
        AgentConfig config = findOneAgentConfig(agentId, category, type);
        if (config == null) {
            throw new IllegalStateException("未找到对应的配置");
        }
        return config.getConfigId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        mapper.deleteByIds(ids);
    }

    private AgentConfigEntity toEntity(AgentConfig config) {
        return BeanUtil.copyProperties(config, AgentConfigEntity.class);
    }

    private AgentConfig toDomain(AgentConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, AgentConfig.class);
    }
}
