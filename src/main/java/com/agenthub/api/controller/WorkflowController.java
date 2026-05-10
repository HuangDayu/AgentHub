package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateWorkflowRequest;
import com.agenthub.api.dto.WorkflowResponse;
import com.agenthub.application.command.WorkflowCommand;
import com.agenthub.application.dto.WorkflowOutput;
import com.agenthub.application.usecase.WorkflowUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/workflows")
public class WorkflowController {
    private final WorkflowUseCase useCase;

    public WorkflowController(WorkflowUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowResponse create(@RequestBody CreateWorkflowRequest request) {
        WorkflowOutput result = useCase.create(BeanUtil.copyProperties(request, WorkflowCommand.class));
        return toResponse(result);
    }

    @GetMapping
    public List<WorkflowResponse> list() {
        return useCase.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{workflowId}")
    public WorkflowResponse get(@PathVariable String workflowId) {
        return toResponse(useCase.get(workflowId));
    }

    @PutMapping("/{workflowId}")
    public WorkflowResponse update(@PathVariable String workflowId,
                                   @RequestBody CreateWorkflowRequest request) {
        WorkflowOutput result = useCase.update(workflowId, BeanUtil.copyProperties(request, WorkflowCommand.class));
        return toResponse(result);
    }

    @PostMapping("/{workflowId}/publish")
    public WorkflowResponse publish(@PathVariable String workflowId) {
        return toResponse(useCase.publish(workflowId));
    }

    @PostMapping("/{workflowId}/unpublish")
    public WorkflowResponse unpublish(@PathVariable String workflowId) {
        return toResponse(useCase.unpublish(workflowId));
    }

    @DeleteMapping("/{workflowId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workflowId) {
        useCase.delete(workflowId);
    }

    private WorkflowResponse toResponse(WorkflowOutput result) {
        return BeanUtil.copyProperties(result, WorkflowResponse.class);
    }
}
