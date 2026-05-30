package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.ScheduledTask;

import java.util.List;
import java.util.Optional;

public interface ScheduledTaskRepository {
    ScheduledTask saveOrUpdate(ScheduledTask task);
    Optional<ScheduledTask> findById(String id);
    List<ScheduledTask> findByWorkspaceId(String workspaceId);
    List<ScheduledTask> findAllEnabled();
    void deleteById(String id);
}
