package com.agenthub.api.controller;

import com.agenthub.api.dto.CreateScheduledTaskRequest;
import com.agenthub.api.dto.ScheduledTaskResponse;
import com.agenthub.api.dto.UpdateScheduledTaskRequest;
import com.agenthub.application.dto.ScheduledTaskOutput;
import com.agenthub.application.usecase.ScheduledTaskUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/scheduled-tasks")
public class ScheduledTaskController {
    private final ScheduledTaskUseCase useCase;

    public ScheduledTaskController(ScheduledTaskUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduledTaskResponse create(@RequestBody CreateScheduledTaskRequest request) {
        ScheduledTaskOutput result = useCase.create(request.tenantId(), request.workspaceId(),
                request.taskCode(), request.name(), request.description(),
                request.taskType(), request.cronExpression(), request.executorConfig(), request.prompt());
        return toResponse(result);
    }

    @GetMapping
    public List<ScheduledTaskResponse> list(@PathVariable String workspaceId) {
        return useCase.list(workspaceId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{taskId}")
    public ScheduledTaskResponse get(@PathVariable String taskId) {
        return toResponse(useCase.get(taskId));
    }

    @PutMapping("/{taskId}")
    public ScheduledTaskResponse update(@PathVariable String taskId,
                                        @RequestBody UpdateScheduledTaskRequest request) {
        ScheduledTaskOutput result = useCase.update(taskId, request.name(),
                request.description(), request.cronExpression(), request.executorConfig(), request.prompt());
        return toResponse(result);
    }

    @PostMapping("/{taskId}/enable")
    public ScheduledTaskResponse enable(@PathVariable String taskId) {
        return toResponse(useCase.enable(taskId));
    }

    @PostMapping("/{taskId}/disable")
    public ScheduledTaskResponse disable(@PathVariable String taskId) {
        return toResponse(useCase.disable(taskId));
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String taskId) {
        useCase.delete(taskId);
    }

    @PostMapping("/{taskId}/execute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void execute(@PathVariable String taskId) {
        useCase.execute(taskId);
    }

    private ScheduledTaskResponse toResponse(ScheduledTaskOutput output) {
        return new ScheduledTaskResponse(output.id(), output.tenantId(), output.workspaceId(),
                output.taskCode(), output.name(), output.description(), output.taskType(),
                output.cronExpression(), output.executorConfig(), output.prompt(),
                output.enabled(), output.lastExecuteTime(), output.nextExecuteTime(),
                output.status(), output.createdAt(), output.updatedAt());
    }
}
