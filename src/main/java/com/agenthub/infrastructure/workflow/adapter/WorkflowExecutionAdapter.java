package com.agenthub.infrastructure.workflow.adapter;

import com.agenthub.application.port.out.workflow.WorkflowExecutionPort;
import com.agenthub.domain.enums.workflow.WorkflowStatus;
import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.WorkflowGraph;
import com.agenthub.infrastructure.workflow.engine.WorkflowEngine;
import com.agenthub.infrastructure.workflow.state.WorkflowStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 工作流执行端口适配器。
 * 实现WorkflowExecutionPort接口，调用WorkflowEngine执行工作流。
 *
 * @author huangdayu
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class WorkflowExecutionAdapter implements WorkflowExecutionPort {

    private final WorkflowEngine workflowEngine;
    private final WorkflowStateManager stateManager;

    /**
     * 初始化执行上下文。
     *
     * @param workflowId 工作流ID
     * @param input 输入参数
     * @return 执行上下文
     */
    @Override
    public Mono<WorkflowContext> initializeContext(String workflowId, Map<String, Object> input) {
        return Mono.fromSupplier(() -> createContext(workflowId, input))
            .flatMap(context -> stateManager.saveContext(context).thenReturn(context));
    }

    /**
     * 创建执行上下文。
     *
     * @param workflowId 工作流ID
     * @param input 输入参数
     * @return 执行上下文
     */
    private WorkflowContext createContext(String workflowId, Map<String, Object> input) {
        WorkflowContext context = WorkflowContext.create(workflowId);
        context.setGraph(WorkflowGraph.empty());
        if (input != null) {
            input.forEach(context::setVariable);
        }
        return context;
    }

    /**
     * 执行单个节点。
     *
     * @param context 执行上下文
     * @param nodeId 节点ID
     * @return 节点执行结果流
     */
    @Override
    public Flux<NodeResult> executeNode(WorkflowContext context, String nodeId) {
        return workflowEngine.execute(context)
            .filter(result -> result.getNodeId().equals(nodeId));
    }

    /**
     * 执行整个工作流。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    @Override
    public Flux<NodeResult> executeWorkflow(WorkflowContext context) {
        return executeWithLifecycle(context);
    }

    /**
     * 带生命周期管理的执行。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    private Flux<NodeResult> executeWithLifecycle(WorkflowContext context) {
        return Flux.defer(() -> startExecution(context))
            .concatMap(ctx -> runWorkflow(ctx))
            .doOnTerminate(() -> endExecution(context));
    }

    /**
     * 开始执行。
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    private Mono<WorkflowContext> startExecution(WorkflowContext context) {
        return stateManager.updateStatus(context.getExecutionId(), WorkflowStatus.EXECUTING)
            .thenReturn(context);
    }

    /**
     * 运行工作流。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    private Flux<NodeResult> runWorkflow(WorkflowContext context) {
        return workflowEngine.execute(context)
            .doOnNext(result -> saveNodeResult(context, result));
    }

    /**
     * 保存节点结果。
     *
     * @param context 执行上下文
     * @param result 节点结果
     */
    private void saveNodeResult(WorkflowContext context, NodeResult result) {
        stateManager.saveNodeResult(context.getExecutionId(), result).subscribe();
    }

    /**
     * 结束执行。
     *
     * @param context 执行上下文
     */
    private void endExecution(WorkflowContext context) {
        WorkflowStatus finalStatus = determineFinalStatus(context);
        stateManager.updateStatus(context.getExecutionId(), finalStatus).subscribe();
    }

    /**
     * 确定最终状态。
     *
     * @param context 执行上下文
     * @return 最终状态
     */
    private WorkflowStatus determineFinalStatus(WorkflowContext context) {
        boolean hasFailure = context.getNodeResults().values().stream()
            .anyMatch(result -> !result.isSuccess());
        return hasFailure ? WorkflowStatus.FAILED : WorkflowStatus.SUCCESS;
    }

    /**
     * 停止执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    @Override
    public Mono<Void> stopExecution(String executionId) {
        return workflowEngine.stop(executionId);
    }
}
