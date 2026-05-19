package com.agenthub.application.port.out.workflow;

import com.agenthub.domain.model.workflow.WorkflowContext;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 工作流状态端口接口。
 * 定义工作流状态持久化的外部能力。
 *
 * @author huangdayu
 */
public interface WorkflowStatePort {

    /**
     * 保存执行上下文。
     *
     * @param context 执行上下文
     * @return 保存后的上下文
     */
    Mono<WorkflowContext> saveContext(WorkflowContext context);

    /**
     * 加载执行上下文。
     *
     * @param executionId 执行ID
     * @return 执行上下文
     */
    Mono<Optional<WorkflowContext>> loadContext(String executionId);

    /**
     * 删除执行上下文。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    Mono<Void> deleteContext(String executionId);

    /**
     * 更新节点状态。
     *
     * @param executionId 执行ID
     * @param nodeId 节点ID
     * @param status 新状态
     * @return 完成信号
     */
    Mono<Void> updateNodeStatus(String executionId, String nodeId, 
                                com.agenthub.domain.enums.workflow.NodeStatus status);
}
