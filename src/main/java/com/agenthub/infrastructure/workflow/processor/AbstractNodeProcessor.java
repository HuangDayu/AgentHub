package com.agenthub.infrastructure.workflow.processor;

import com.agenthub.domain.enums.workflow.DagNodeStatus;
import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.DagWorkflowContext;
import com.agenthub.domain.model.workflow.DagWorkflowNode;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * 抽象节点处理器。
 * 使用模板方法模式定义节点处理的标准流程。
 *
 * @author huangdayu
 */
@Slf4j
public abstract class AbstractNodeProcessor implements NodeProcessor {

    /**
     * 处理节点执行（模板方法）。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 处理结果的Mono
     */
    @Override
    public Mono<NodeResult> process(DagWorkflowNode node, DagWorkflowContext context) {
        log.info("开始执行节点: {} [{}]", node.getName(), node.getId());
        Instant startTime = Instant.now();
        
        return validate(node)
            .then(Mono.defer(() -> doProcess(node, context)))
            .map(outputs -> createSuccessResult(node, outputs, startTime))
            .onErrorResume(error -> handleFailure(node, error, startTime))
            .doOnNext(result -> logResult(node, result));
    }

    /**
     * 执行具体的节点处理逻辑。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 输出数据的Mono
     */
    protected abstract Mono<Map<String, Object>> doProcess(
            DagWorkflowNode node, DagWorkflowContext context);

    /**
     * 创建成功的执行结果。
     *
     * @param node 工作流节点
     * @param outputs 输出数据
     * @param startTime 开始时间
     * @return 节点结果
     */
    private NodeResult createSuccessResult(DagWorkflowNode node, 
                                           Map<String, Object> outputs, 
                                           Instant startTime) {
        NodeResult result = NodeResult.success(node.getId(), outputs);
        result.setStartTime(startTime);
        result.setEndTime(Instant.now());
        result.setDurationMs(result.calculateDuration());
        return result;
    }

    /**
     * 处理执行失败。
     *
     * @param node 工作流节点
     * @param error 错误信息
     * @param startTime 开始时间
     * @return 失败结果的Mono
     */
    private Mono<NodeResult> handleFailure(DagWorkflowNode node, 
                                           Throwable error, 
                                           Instant startTime) {
        log.error("节点执行失败: {} [{}]", node.getName(), node.getId(), error);
        NodeResult result = NodeResult.failure(node.getId(), error.getMessage());
        result.setStartTime(startTime);
        result.setEndTime(Instant.now());
        result.setDurationMs(result.calculateDuration());
        return Mono.just(result);
    }

    /**
     * 记录执行结果日志。
     *
     * @param node 工作流节点
     * @param result 执行结果
     */
    private void logResult(DagWorkflowNode node, NodeResult result) {
        if (result.isSuccess()) {
            log.info("节点执行成功: {} [{}], 耗时: {}ms", 
                node.getName(), node.getId(), result.getDurationMs());
        } else {
            log.warn("节点执行失败: {} [{}], 原因: {}", 
                node.getName(), node.getId(), result.getErrorMessage());
        }
    }

    /**
     * 默认验证实现。
     *
     * @param node 工作流节点
     * @return 验证结果的Mono
     */
    @Override
    public Mono<Void> validate(DagWorkflowNode node) {
        return Mono.fromRunnable(() -> validateNodeConfig(node));
    }

    /**
     * 验证节点配置。
     *
     * @param node 工作流节点
     */
    protected void validateNodeConfig(DagWorkflowNode node) {
        if (node.getConfig() == null) {
            throw new IllegalArgumentException("节点配置不能为空");
        }
    }

    /**
     * 更新节点状态。
     *
     * @param node 工作流节点
     * @param status 新状态
     */
    protected void updateDagNodeStatus(DagWorkflowNode node, DagNodeStatus status) {
        node.updateStatus(status);
    }
}
