package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.domain.model.ScheduledTask;
import com.agenthub.infrastructure.store.db.entity.ScheduledTaskEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ScheduledTaskMapper {
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
