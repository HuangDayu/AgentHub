package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.ScheduledTaskRepository;
import com.agenthub.domain.model.ScheduledTask;
import com.agenthub.infrastructure.store.db.entity.ScheduledTaskEntity;
import com.agenthub.infrastructure.store.db.mapper.ScheduledTaskMapper;
import com.agenthub.infrastructure.store.db.mapper.ScheduledTaskMyBatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisScheduledTaskRepository implements ScheduledTaskRepository {
    private final ScheduledTaskMyBatisMapper mapper;

    public MybatisScheduledTaskRepository(ScheduledTaskMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ScheduledTask saveOrUpdate(ScheduledTask task) {
        ScheduledTaskEntity entity = ScheduledTaskMapper.toEntity(task);
        mapper.insertOrUpdate(entity);
        return BeanUtil.copyProperties(entity, ScheduledTask.class);
    }

    @Override
    public Optional<ScheduledTask> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id))
                .map(ScheduledTaskMapper::toDomain);
    }

    @Override
    public List<ScheduledTask> findByWorkspaceId(String workspaceId) {
        LambdaQueryWrapper<ScheduledTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduledTaskEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(queryWrapper).stream()
                .map(ScheduledTaskMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }
}
