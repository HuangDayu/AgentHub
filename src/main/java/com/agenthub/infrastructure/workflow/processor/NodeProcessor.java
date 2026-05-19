package com.agenthub.infrastructure.workflow.processor;

import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.WorkflowNode;
import reactor.core.publisher.Mono;

/**
 * 节点处理器接口。
 * 定义工作流节点的处理契约。
 *
 * @author huangdayu
 */
public interface NodeProcessor {

    /**
     * 获取处理器支持的节点类型。
     *
     * @return 节点类型
     */
    String getSupportedType();

    /**
     * 处理节点执行。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 处理结果的Mono
     */
    Mono<NodeResult> process(WorkflowNode node, WorkflowContext context);

    /**
     * 判断是否支持指定节点。
     *
     * @param node 工作流节点
     * @return 如果支持返回true
     */
    default boolean supports(WorkflowNode node) {
        return getSupportedType().equals(node.getType().name());
    }

    /**
     * 验证节点配置。
     *
     * @param node 工作流节点
     * @return 验证结果的Mono
     */
    Mono<Void> validate(WorkflowNode node);
}
