package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
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

    public static ScheduledTaskEntity toEntity(ScheduledTask task) {
        ScheduledTaskEntity entity = new ScheduledTaskEntity();
        entity.setId(task.getId());
        entity.setTenantId(task.getTenantId());
        entity.setWorkspaceId(task.getWorkspaceId());
        entity.setTaskCode(task.getTaskCode());
        entity.setName(task.getName());
        entity.setDescription(task.getDescription());
        entity.setTaskType(task.getTaskType());
        entity.setCronExpression(task.getCronExpression());
        entity.setExecutorConfig(task.getExecutorConfig());
        entity.setPrompt(task.getPrompt());
        entity.setEnabled(task.isEnabled());
        entity.setLastExecuteTime(toInstant(task.getLastExecuteTime()));
        entity.setNextExecuteTime(toInstant(task.getNextExecuteTime()));
        entity.setStatus(task.getStatus());
        entity.setCreatedAt(toInstant(task.getCreatedAt()));
        entity.setUpdatedAt(toInstant(task.getUpdatedAt()));
        return entity;
    }

    public static ScheduledTask toDomain(ScheduledTaskEntity entity) {
        ScheduledTask task = new ScheduledTask();
        task.setId(entity.getId());
        task.setTenantId(entity.getTenantId());
        task.setWorkspaceId(entity.getWorkspaceId());
        task.setTaskCode(entity.getTaskCode());
        task.setName(entity.getName());
        task.setDescription(entity.getDescription());
        task.setTaskType(entity.getTaskType());
        task.setCronExpression(entity.getCronExpression());
        task.setExecutorConfig(entity.getExecutorConfig());
        task.setPrompt(entity.getPrompt());
        task.setEnabled(entity.isEnabled());
        task.setLastExecuteTime(toLocalDateTime(entity.getLastExecuteTime()));
        task.setNextExecuteTime(toLocalDateTime(entity.getNextExecuteTime()));
        task.setStatus(entity.getStatus());
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
