package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateScheduledTaskRequest;
import com.agenthub.api.dto.ScheduledTaskResponse;
import com.agenthub.api.dto.UpdateScheduledTaskRequest;
import com.agenthub.application.command.ScheduledTaskCommand;
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
        ScheduledTaskOutput result = useCase.create(BeanUtil.copyProperties(request, ScheduledTaskCommand.class));
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
        ScheduledTaskOutput result = useCase.update(taskId, BeanUtil.copyProperties(request, ScheduledTaskCommand.class));
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
        return BeanUtil.copyProperties(output, ScheduledTaskResponse.class);
    }
}
