package com.agenthub.application.port.out.workflow;

import com.agenthub.application.command.ExecutionCommand;
import com.agenthub.domain.model.workflow.DagWorkflowContext;
import com.agenthub.domain.model.workflow.NodeResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 工作流执行端口接口。
 * 定义工作流执行的外部能力。
 *
 * @author huangdayu
 */
public interface DagWorkflowExecutionPort {

    /**
     * 初始化执行上下文。
     *
     * @return 执行上下文
     */
    Mono<DagWorkflowContext> initializeContext(ExecutionCommand command);

    /**
     * 执行单个节点。
     *
     * @param context 执行上下文
     * @param nodeId 节点ID
     * @return 节点执行结果流
     */
    Flux<NodeResult> executeNode(DagWorkflowContext context, String nodeId);

    /**
     * 执行整个工作流。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    Flux<NodeResult> executeWorkflow(DagWorkflowContext context);

    /**
     * 停止执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    Mono<Void> stopExecution(String executionId);
}
