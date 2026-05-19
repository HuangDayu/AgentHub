package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.ExecuteWorkflowRequest;
import com.agenthub.api.dto.ExecutionResponse;
import com.agenthub.api.dto.NodeExecutionEventResponse;
import com.agenthub.application.command.workflow.ExecutionCommand;
import com.agenthub.application.dto.workflow.ExecutionOutput;
import com.agenthub.application.usecase.WorkflowExecutionUseCase;
import com.agenthub.domain.model.workflow.NodeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 工作流执行控制器。
 * 提供工作流执行相关的REST API。
 *
 * @author huangdayu
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/workflows")
@RequiredArgsConstructor
public class WorkflowExecutionController {

    private final WorkflowExecutionUseCase executionUseCase;

    /**
     * 执行工作流（SSE流式返回）。
     *
     * @param workspaceId 工作空间ID
     * @param workflowId 工作流ID
     * @param request 执行请求
     * @return SSE事件流
     */
    @PostMapping("/{workflowId}/execute")
    public Flux<ServerSentEvent<NodeExecutionEventResponse>> execute(
            @PathVariable String workspaceId,
            @PathVariable String workflowId,
            @RequestBody ExecuteWorkflowRequest request) {
        ExecutionCommand command = toCommand(workspaceId, workflowId, request);
        return executionUseCase.execute(command)
                .map(v -> BeanUtil.copyProperties(v, NodeExecutionEventResponse.class))
                .map(this::toSseEvent);
    }

    /**
     * 停止工作流执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    @PostMapping("/executions/{executionId}/stop")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public Mono<Void> stop(@PathVariable String executionId) {
        return executionUseCase.stop(executionId);
    }

    /**
     * 获取执行结果。
     *
     * @param executionId 执行ID
     * @return 执行响应
     */
    @GetMapping("/executions/{executionId}")
    public Mono<ExecutionResponse> getResult(@PathVariable String executionId) {
        return executionUseCase.getResult(executionId)
                .map(this::toResponse);
    }

    /**
     * 转换为执行命令。
     */
    private ExecutionCommand toCommand(String workspaceId, String workflowId, 
                                        ExecuteWorkflowRequest request) {
        ExecutionCommand cmd = new ExecutionCommand();
        cmd.setWorkflowId(workflowId);
        cmd.setWorkspaceId(workspaceId);
        cmd.setInput(request.getInput());
        cmd.setTriggeredBy(request.getTriggeredBy());
        return cmd;
    }

    /**
     * 转换为SSE事件。
     */
    private ServerSentEvent<NodeExecutionEventResponse> toSseEvent(
            NodeExecutionEventResponse response) {
        return ServerSentEvent.<NodeExecutionEventResponse>builder()
                .event(response.getEventType())
                .data(response)
                .build();
    }

    /**
     * 转换为响应DTO。
     */
    private ExecutionResponse toResponse(ExecutionOutput output) {
        return BeanUtil.copyProperties(output, ExecutionResponse.class);
    }
}
