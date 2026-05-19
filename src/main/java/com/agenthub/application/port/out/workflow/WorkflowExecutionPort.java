package com.agenthub.application.port.out.workflow;

import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.NodeResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 工作流执行端口接口。
 * 定义工作流执行的外部能力。
 *
 * @author huangdayu
 */
public interface WorkflowExecutionPort {

    /**
     * 初始化执行上下文。
     *
     * @param workflowId 工作流ID
     * @param input 输入参数
     * @return 执行上下文
     */
    Mono<WorkflowContext> initializeContext(String workflowId, java.util.Map<String, Object> input);

    /**
     * 执行单个节点。
     *
     * @param context 执行上下文
     * @param nodeId 节点ID
     * @return 节点执行结果流
     */
    Flux<NodeResult> executeNode(WorkflowContext context, String nodeId);

    /**
     * 执行整个工作流。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    Flux<NodeResult> executeWorkflow(WorkflowContext context);

    /**
     * 停止执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    Mono<Void> stopExecution(String executionId);
}
