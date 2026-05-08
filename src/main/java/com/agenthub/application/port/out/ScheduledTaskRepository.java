package com.agenthub.application.port.out;

import com.agenthub.domain.model.ScheduledTask;

import java.util.List;
import java.util.Optional;

public interface ScheduledTaskRepository {
    ScheduledTask save(ScheduledTask task);
    Optional<ScheduledTask> findById(String id);
    List<ScheduledTask> findByWorkspaceId(String workspaceId);
    void deleteById(String id);
}
