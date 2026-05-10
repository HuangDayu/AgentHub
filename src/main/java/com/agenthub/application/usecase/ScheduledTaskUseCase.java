package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.ScheduledTaskCommand;
import com.agenthub.application.dto.ScheduledTaskOutput;
import com.agenthub.application.port.out.ScheduledTaskRepository;
import com.agenthub.domain.model.ScheduledTask;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledTaskUseCase {
    private final ScheduledTaskRepository repository;

    public ScheduledTaskUseCase(ScheduledTaskRepository repository) {
        this.repository = repository;
    }

    public ScheduledTaskOutput create(ScheduledTaskCommand command) {
        ScheduledTask task = BeanUtil.copyProperties(command, ScheduledTask.class);
        return toOutput(repository.saveOrUpdate(task));
    }

    public ScheduledTaskOutput get(String id) {
        return repository.findById(id).map(this::toOutput).orElse(null);
    }

    public List<ScheduledTaskOutput> list(String workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream().map(this::toOutput).toList();
    }

    public ScheduledTaskOutput update(String id, ScheduledTaskCommand command) {
        ScheduledTask task = BeanUtil.copyProperties(command, ScheduledTask.class);
        task.setId(id);
        return toOutput(repository.saveOrUpdate(task));
    }

    public ScheduledTaskOutput enable(String id) {
        return repository.findById(id).map(task -> {
            task.setEnabled(true);
            return toOutput(repository.saveOrUpdate(task));
        }).orElse(null);
    }

    public ScheduledTaskOutput disable(String id) {
        return repository.findById(id).map(task -> {
            task.setEnabled(false);
            return toOutput(repository.saveOrUpdate(task));
        }).orElse(null);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public void execute(String id) {
        repository.findById(id).ifPresent(task -> {
            task.setLastExecuteTime(LocalDateTime.now());
            task.setStatus("RUNNING");
            repository.saveOrUpdate(task);
        });
    }


    private ScheduledTaskOutput toOutput(ScheduledTask task) {
        return new ScheduledTaskOutput(task.getId(), task.getTenantId(), task.getWorkspaceId(),
                task.getTaskCode(), task.getName(), task.getDescription(), task.getTaskType(),
                task.getCronExpression(), task.getExecutorConfig(), task.getPrompt(),
                task.isEnabled(), task.getLastExecuteTime(), task.getNextExecuteTime(),
                task.getStatus(), task.getCreatedAt(), task.getUpdatedAt());
    }
}
