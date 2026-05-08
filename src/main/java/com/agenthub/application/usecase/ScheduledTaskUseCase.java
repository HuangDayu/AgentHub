package com.agenthub.application.usecase;

import com.agenthub.application.dto.ScheduledTaskOutput;
import com.agenthub.application.port.out.ScheduledTaskRepository;
import com.agenthub.domain.model.ScheduledTask;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ScheduledTaskUseCase {
    private final ScheduledTaskRepository repository;

    public ScheduledTaskUseCase(ScheduledTaskRepository repository) {
        this.repository = repository;
    }

    public ScheduledTaskOutput create(String tenantId, String workspaceId, String taskCode,
                                     String name, String description, String taskType,
                                     String cronExpression, String executorConfig, String prompt) {
        ScheduledTask task = buildTask(tenantId, workspaceId, taskCode, name, description,
                                       taskType, cronExpression, executorConfig, prompt);
        return toOutput(repository.save(task));
    }

    public ScheduledTaskOutput get(String id) {
        return repository.findById(id).map(this::toOutput).orElse(null);
    }

    public List<ScheduledTaskOutput> list(String workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream().map(this::toOutput).toList();
    }

    public ScheduledTaskOutput update(String id, String name, String description,
                                      String cronExpression, String executorConfig, String prompt) {
        return repository.findById(id).map(task -> {
            updateTask(task, name, description, cronExpression, executorConfig, prompt);
            return toOutput(repository.save(task));
        }).orElse(null);
    }

    public ScheduledTaskOutput enable(String id) {
        return repository.findById(id).map(task -> {
            task.setEnabled(true);
            return toOutput(repository.save(task));
        }).orElse(null);
    }

    public ScheduledTaskOutput disable(String id) {
        return repository.findById(id).map(task -> {
            task.setEnabled(false);
            return toOutput(repository.save(task));
        }).orElse(null);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public void execute(String id) {
        repository.findById(id).ifPresent(task -> {
            task.setLastExecuteTime(LocalDateTime.now());
            task.setStatus("RUNNING");
            repository.save(task);
        });
    }

    private ScheduledTask buildTask(String tenantId, String workspaceId, String taskCode,
                                    String name, String description, String taskType,
                                    String cronExpression, String executorConfig, String prompt) {
        ScheduledTask task = new ScheduledTask();
        task.setId(UUID.randomUUID().toString());
        task.setTenantId(tenantId);
        task.setWorkspaceId(workspaceId);
        task.setTaskCode(taskCode);
        task.setName(name);
        task.setDescription(description);
        task.setTaskType(taskType);
        task.setCronExpression(cronExpression);
        task.setExecutorConfig(executorConfig);
        task.setPrompt(prompt);
        task.setEnabled(true);
        task.setStatus("CREATED");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private void updateTask(ScheduledTask task, String name, String description,
                           String cronExpression, String executorConfig, String prompt) {
        task.setName(name);
        task.setDescription(description);
        task.setCronExpression(cronExpression);
        task.setExecutorConfig(executorConfig);
        task.setPrompt(prompt);
        task.setUpdatedAt(LocalDateTime.now());
    }

    private ScheduledTaskOutput toOutput(ScheduledTask task) {
        return new ScheduledTaskOutput(task.getId(), task.getTenantId(), task.getWorkspaceId(),
                task.getTaskCode(), task.getName(), task.getDescription(), task.getTaskType(),
                task.getCronExpression(), task.getExecutorConfig(), task.getPrompt(),
                task.isEnabled(), task.getLastExecuteTime(), task.getNextExecuteTime(),
                task.getStatus(), task.getCreatedAt(), task.getUpdatedAt());
    }
}
