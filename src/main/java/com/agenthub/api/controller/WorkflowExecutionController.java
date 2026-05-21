package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.ExecuteWorkflowRequest;
import com.agenthub.api.dto.ExecutionResponse;
import com.agenthub.application.command.workflow.ExecutionCommand;
import com.agenthub.application.dto.workflow.ExecutionOutput;
import com.agenthub.application.usecase.WorkflowExecutionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 工作流执行控制器。
 * 提供工作流执行相关的REST API。
 *
 * @author huangdayu
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/workflows")
@RequiredArgsConstructor
public class WorkflowExecutionController {

    private final WorkflowExecutionUseCase executionUseCase;

    /**
     * 启动工作流执行（同步返回执行ID，前端轮询获取结果）。
     *
     * @param workspaceId 工作空间ID
     * @param workflowId 工作流ID
     * @param request 执行请求
     * @return 执行响应（含executionId）
     */
    @PostMapping("/{workflowId}/execute")
    public Mono<ExecutionResponse> execute(
            @PathVariable String workspaceId,
            @PathVariable String workflowId,
            @RequestBody ExecuteWorkflowRequest request) {
        ExecutionCommand command = toCommand(workspaceId, workflowId, request);
        // 先初始化执行上下文，获取executionId
        return executionUseCase.initialize(command)
                .flatMap(output -> {
                    // 异步执行工作流（fire-and-forget），使用已初始化的executionId
                    executionUseCase.executeById(output.getExecutionId())
                            .doOnError(e -> log.error("Workflow execution failed: {}", e.getMessage()))
                            .subscribe();
                    return Mono.just(output);
                })
                .map(this::toResponse);
    }

    /**
     * 获取执行结果。
     *
     * @param workflowId 工作流ID
     * @param executionId 执行ID
     * @return 执行响应
     */
    @GetMapping("/{workflowId}/executions/{executionId}")
    public Mono<ExecutionResponse> getResult(@PathVariable String workflowId,
                                              @PathVariable String executionId) {
        return executionUseCase.getResult(executionId)
                .map(this::toResponse);
    }

    /**
     * 停止工作流执行。
     *
     * @param workflowId 工作流ID
     * @param executionId 执行ID
     * @return 完成信号
     */
    @PostMapping("/{workflowId}/executions/{executionId}/stop")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public Mono<Void> stop(@PathVariable String workflowId,
                            @PathVariable String executionId) {
        return executionUseCase.stop(executionId);
    }

    /**
     * 获取工作流执行历史。
     *
     * @param workflowId 工作流ID
     * @param limit 返回数量（可选，默认20）
     * @return 执行历史列表
     */
    @GetMapping("/{workflowId}/executions")
    public Flux<ExecutionResponse> listHistory(
            @PathVariable String workflowId,
            @RequestParam(defaultValue = "20") int limit) {
        return executionUseCase.listHistory(workflowId, limit)
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
     * 转换为响应DTO。
     */
    private ExecutionResponse toResponse(ExecutionOutput output) {
        return BeanUtil.copyProperties(output, ExecutionResponse.class);
    }
}
