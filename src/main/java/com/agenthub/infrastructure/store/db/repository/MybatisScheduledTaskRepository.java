package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.agenthub.application.port.out.repositories.ScheduledTaskRepository;
import com.agenthub.domain.model.ScheduledTask;
import com.agenthub.infrastructure.store.db.entity.ScheduledTaskEntity;
import com.agenthub.infrastructure.store.db.mapper.ScheduledTaskMyBatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        ScheduledTaskEntity entity = toEntity(task);
        mapper.insertOrUpdate(entity);
        return BeanUtil.copyProperties(entity, ScheduledTask.class);
    }

    @Override
    public Optional<ScheduledTask> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id))
                .map(MybatisScheduledTaskRepository::toDomain);
    }

    @Override
    public List<ScheduledTask> findByWorkspaceId(String workspaceId) {
        LambdaQueryWrapper<ScheduledTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduledTaskEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(queryWrapper).stream()
                .map(MybatisScheduledTaskRepository::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public List<ScheduledTask> findAllEnabled() {
        LambdaQueryWrapper<ScheduledTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduledTaskEntity::isEnabled, true);
        return mapper.selectList(queryWrapper).stream()
                .map(MybatisScheduledTaskRepository::toDomain)
                .toList();
    }

    public static ScheduledTaskEntity toEntity(ScheduledTask task) {
        ScheduledTaskEntity entity = new ScheduledTaskEntity();
        BeanUtil.copyProperties(task, entity, CopyOptions.create().setIgnoreProperties("lastExecuteTime", "nextExecuteTime", "createdAt", "updatedAt"));
        entity.setLastExecuteTime(toInstant(task.getLastExecuteTime()));
        entity.setNextExecuteTime(toInstant(task.getNextExecuteTime()));
        entity.setCreatedAt(toInstant(task.getCreatedAt()));
        entity.setUpdatedAt(toInstant(task.getUpdatedAt()));
        return entity;
    }

    public static ScheduledTask toDomain(ScheduledTaskEntity entity) {
        ScheduledTask task = new ScheduledTask();
        BeanUtil.copyProperties(entity, task, CopyOptions.create().setIgnoreProperties("lastExecuteTime", "nextExecuteTime", "createdAt", "updatedAt"));
        task.setLastExecuteTime(toLocalDateTime(entity.getLastExecuteTime()));
        task.setNextExecuteTime(toLocalDateTime(entity.getNextExecuteTime()));
        task.setCreatedAt(toLocalDateTime(entity.getCreatedAt()));
        task.setUpdatedAt(toLocalDateTime(entity.getUpdatedAt()));
        return task;
    }

    private static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
