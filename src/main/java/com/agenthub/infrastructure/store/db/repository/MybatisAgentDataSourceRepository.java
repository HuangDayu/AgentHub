package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.enums.AgentDataSourceStatus;
import com.agenthub.domain.model.datasource.AgentDataSource;
import com.agenthub.infrastructure.store.db.entity.AgentDataSourceEntity;
import com.agenthub.infrastructure.store.db.mapper.AgentDataSourceMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agent 数据源 MyBatis 仓储
 */
@Repository
@Primary
public class MybatisAgentDataSourceRepository implements AgentDataSourceRepository {
    private final AgentDataSourceMybatisMapper mapper;

    public MybatisAgentDataSourceRepository(AgentDataSourceMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AgentDataSource save(AgentDataSource source) {
        AgentDataSourceEntity entity = toEntity(source);
        if (entity.getId() == null || entity.getId().isBlank()
                || mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return toDomain(mapper.selectById(entity.getId()));
    }

    @Override
    public Optional<AgentDataSource> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<AgentDataSource> findByWorkspaceId(String workspaceId) {
        LambdaQueryWrapper<AgentDataSourceEntity> q = new LambdaQueryWrapper<>();
        q.eq(AgentDataSourceEntity::getWorkspaceId, workspaceId)
         .orderByAsc(AgentDataSourceEntity::getCreatedAt);
        return mapper.selectList(q).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AgentDataSource> findAll() {
        LambdaQueryWrapper<AgentDataSourceEntity> q = new LambdaQueryWrapper<>();
        return mapper.selectList(q).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsByWorkspaceIdAndName(String workspaceId, String name) {
        LambdaQueryWrapper<AgentDataSourceEntity> q = new LambdaQueryWrapper<>();
        q.eq(AgentDataSourceEntity::getWorkspaceId, workspaceId)
         .eq(AgentDataSourceEntity::getName, name);
        return mapper.selectCount(q) > 0;
    }

    private AgentDataSourceEntity toEntity(AgentDataSource s) {
        AgentDataSourceEntity e = new AgentDataSourceEntity();
        BeanUtil.copyProperties(s, e);
        if (s.getProtocol() != null) e.setProtocol(s.getProtocol().name());
        if (s.getStatus() != null) e.setStatus(s.getStatus().name());
        if (s.isEnabled()) e.setEnabled(true);
        else e.setEnabled(false);
        return e;
    }

    private AgentDataSource toDomain(AgentDataSourceEntity e) {
        if (e == null) return null;
        AgentDataSource s = new AgentDataSource();
        BeanUtil.copyProperties(e, s);
        if (e.getProtocol() != null) s.setProtocol(AgentDataSourceProtocol.valueOf(e.getProtocol()));
        if (e.getStatus() != null) s.setStatus(AgentDataSourceStatus.valueOf(e.getStatus()));
        s.setEnabled(Boolean.TRUE.equals(e.getEnabled()));
        return s;
    }
}
