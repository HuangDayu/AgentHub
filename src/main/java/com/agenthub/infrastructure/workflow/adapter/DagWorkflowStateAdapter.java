package com.agenthub.infrastructure.workflow.adapter;

import com.agenthub.application.port.out.workflow.DagWorkflowStatePort;
import com.agenthub.domain.enums.workflow.DagNodeStatus;
import com.agenthub.domain.enums.workflow.DagWorkflowStatus;
import com.agenthub.domain.model.workflow.DagWorkflowContext;
import com.agenthub.infrastructure.workflow.state.DagWorkflowStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 工作流状态端口适配器。
 * 实现WorkflowStatePort接口，调用WorkflowStateManager管理状态。
 *
 * @author huangdayu
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class DagWorkflowStateAdapter implements DagWorkflowStatePort {

    private final DagWorkflowStateManager stateManager;

    /**
     * 保存执行上下文。
     *
     * @param context 执行上下文
     * @return 保存后的上下文
     */
    @Override
    public Mono<DagWorkflowContext> saveContext(DagWorkflowContext context) {
        return stateManager.saveContext(context).thenReturn(context);
    }

    /**
     * 加载执行上下文。
     *
     * @param executionId 执行ID
     * @return 执行上下文
     */
    @Override
    public Mono<Optional<DagWorkflowContext>> loadContext(String executionId) {
        return stateManager.loadContext(executionId)
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty());
    }

    /**
     * 删除执行上下文。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    @Override
    public Mono<Void> deleteContext(String executionId) {
        return stateManager.deleteState(executionId);
    }

    /**
     * 更新节点状态。
     *
     * @param executionId 执行ID
     * @param nodeId 节点ID
     * @param status 新状态
     * @return 完成信号
     */
    @Override
    public Mono<Void> updateNodeStatus(String executionId, String nodeId, DagNodeStatus status) {
        return stateManager.updateNodeStatus(executionId, nodeId, status);
    }

    @Override
    public Flux<DagWorkflowContext> listContexts(String workflowId, int limit) {
        return stateManager.listContexts(workflowId, limit);
    }
}
