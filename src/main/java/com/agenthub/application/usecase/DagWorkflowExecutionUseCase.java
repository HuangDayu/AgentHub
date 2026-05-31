package com.agenthub.application.usecase;

import com.agenthub.application.command.ExecutionCommand;
import com.agenthub.application.dto.workflow.DagExecutionOutput;
import com.agenthub.application.port.out.workflow.DagWorkflowExecutionPort;
import com.agenthub.application.port.out.workflow.DagWorkflowStatePort;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.DagWorkflowContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工作流执行用例。
 * 负责工作流的执行、停止和结果获取。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class DagWorkflowExecutionUseCase {

    private final DagWorkflowExecutionPort executionPort;
    private final DagWorkflowStatePort statePort;

    /**
     * 初始化工作流执行。
     *
     * @param command 执行命令
     * @return 执行输出
     */
    public Mono<DagExecutionOutput> initialize(ExecutionCommand command) {
        return executionPort.initializeContext(command)
                .flatMap(statePort::saveContext)
                .map(this::toOutput);
    }

    /**
     * 执行工作流并返回节点执行事件流。
     *
     * @param command 执行命令
     * @return 节点执行结果流
     */
    public Flux<NodeResult> execute(ExecutionCommand command) {
        return executionPort.initializeContext(command)
                .flatMapMany(executionPort::executeWorkflow);
    }

    /**
     * 根据executionId执行工作流。
     *
     * @param executionId 执行ID
     * @return 节点执行结果流
     */
    public Flux<NodeResult> executeById(String executionId) {
        return statePort.loadContext(executionId)
                .flatMapMany(opt -> executeWorkflowFromContext(opt, executionId));
    }

    /**
     * 从Optional<DagWorkflowContext>执行工作流。
     */
    private Flux<NodeResult> executeWorkflowFromContext(Optional<DagWorkflowContext> opt, String executionId) {
        DagWorkflowContext context = opt.orElseThrow(() -> 
                new NotFoundException("Execution not found: " + executionId));
        return executionPort.executeWorkflow(context);
    }

    /**
     * 停止工作流执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    public Mono<Void> stop(String executionId) {
        return executionPort.stopExecution(executionId)
                .then(statePort.deleteContext(executionId));
    }

    /**
     * 获取执行结果。
     *
     * @param executionId 执行ID
     * @return 执行输出
     */
    public Mono<DagExecutionOutput> getResult(String executionId) {
        return statePort.loadContext(executionId)
                .map(opt -> opt.orElseThrow(() -> 
                        new NotFoundException("Execution not found: " + executionId)))
                .map(this::toOutput);
    }

    /**
     * 获取工作流执行历史。
     *
     * @param workflowId 工作流ID
     * @param limit 最大返回数量
     * @return 执行输出列表
     */
    public Flux<DagExecutionOutput> listHistory(String workflowId, int limit) {
        return statePort.listContexts(workflowId, limit)
                .map(this::toOutput);
    }

    /**
     * 将后端NodeStatus枚举值映射为前端TaskStatus字符串。
     */
    private String mapNodeStatus(com.agenthub.domain.enums.workflow.DagNodeStatus status) {
        if (status == null) return "pending";
        switch (status) {
            case PENDING: case WAITING: return "pending";
            case EXECUTING: return "running";
            case SUCCESS: return "success";
            case FAILED: return "failed";
            case SKIPPED: return "skipped";
            case CANCELLED: return "cancelled";
            case TIMEOUT: return "timeout";
            default: return status.name().toLowerCase();
        }
    }

    /**
     * 将后端DagWorkflowStatus枚举值映射为前端TaskStatus字符串。
     */
    private String mapWorkflowStatus(com.agenthub.domain.enums.workflow.DagWorkflowStatus status) {
        if (status == null) return "pending";
        switch (status) {
            case DRAFT: return "pending";
            case PUBLISHED: return "success";
            case EXECUTING: return "running";
            case SUCCESS: return "success";
            case FAILED: return "failed";
            case PAUSED: return "pending";
            case CANCELLED: return "cancelled";
            default: return status.name().toLowerCase();
        }
    }

    /**
     * 转换为输出对象（含状态映射和结果转换）。
     */
    private DagExecutionOutput toOutput(DagWorkflowContext context) {
        // 不使用 BeanUtil.copyProperties，因为 status 和 nodeResults 字段类型不匹配会导致异常
        DagExecutionOutput output = new DagExecutionOutput();
        output.setExecutionId(context.getExecutionId());
        output.setWorkflowId(context.getWorkflowId());
        // 将状态转为小写以匹配前端TaskStatus格式
        if (context.getStatus() != null) {
            output.setStatus(mapWorkflowStatus(context.getStatus()));
        } else {
            output.setStatus("pending");
        }
        output.setVariables(context.getVariables());
        output.setStartTime(context.getStartTime());
        output.setEndTime(context.getEndTime());
        // 将 Map<String, NodeResult> 转换为前端需要的 List<Map> 格式
        if (context.getNodeResults() != null && !context.getNodeResults().isEmpty()) {
            List<Map<String, Object>> nodeResults = new ArrayList<>();
            context.getNodeResults().forEach((nodeId, result) -> {
                Map<String, Object> item = new HashMap<>();
                item.put("node_id", result.getNodeId());
                item.put("node_name", result.getNodeId()); // fallback, actual name from graph
                item.put("node_type", "");
                item.put("node_status", mapNodeStatus(result.getStatus()));
                item.put("output", result.getOutputs());
                item.put("error_info", result.getErrorMessage());
                item.put("node_exec_time", result.getStartTime() != null && result.getEndTime() != null
                        ? (result.getEndTime().toEpochMilli() - result.getStartTime().toEpochMilli()) + "ms"
                        : "");
                nodeResults.add(item);
            });
            output.setNodeResults(nodeResults);
        }
        return output;
    }
}
