package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.enums.workflow.DagNodeType;
import com.agenthub.domain.model.workflow.DagWorkflowContext;
import com.agenthub.domain.model.workflow.DagWorkflowNode;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 结束节点处理器。
 * 处理工作流的结束节点，汇总执行结果。
 *
 * @author huangdayu
 */
@Component
public class EndNodeProcessor extends AbstractNodeProcessor {

    /**
     * 获取支持的节点类型。
     *
     * @return END类型
     */
    @Override
    public String getSupportedType() {
        return DagNodeType.END.name();
    }

    /**
     * 执行结束节点处理。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 汇总输出数据的Mono
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(
            DagWorkflowNode node, DagWorkflowContext context) {
        return Mono.fromSupplier(() -> finalizeOutputs(node, context));
    }

    /**
     * 生成最终输出数据。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 最终输出数据
     */
    private Map<String, Object> finalizeOutputs(DagWorkflowNode node, 
                                                 DagWorkflowContext context) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("completed", true);
        outputs.put("endTime", System.currentTimeMillis());
        addFinalVariables(outputs, context);
        addExecutionSummary(outputs, context);
        return outputs;
    }

    /**
     * 添加最终变量到输出。
     *
     * @param outputs 输出数据
     * @param context 执行上下文
     */
    private void addFinalVariables(Map<String, Object> outputs, 
                                    DagWorkflowContext context) {
        if (context.getVariables() != null) {
            outputs.putAll(context.getVariables());
        }
    }

    /**
     * 添加执行摘要信息。
     *
     * @param outputs 输出数据
     * @param context 执行上下文
     */
    private void addExecutionSummary(Map<String, Object> outputs, 
                                      DagWorkflowContext context) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalNodes", context.getNodeResults().size());
        summary.put("successNodes", countSuccessNodes(context));
        summary.put("failedNodes", countFailedNodes(context));
        outputs.put("summary", summary);
    }

    /**
     * 统计成功节点数量。
     *
     * @param context 执行上下文
     * @return 成功节点数量
     */
    private long countSuccessNodes(DagWorkflowContext context) {
        return context.getNodeResults().values().stream()
            .filter(result -> result.isSuccess())
            .count();
    }

    /**
     * 统计失败节点数量。
     *
     * @param context 执行上下文
     * @return 失败节点数量
     */
    private long countFailedNodes(DagWorkflowContext context) {
        return context.getNodeResults().values().stream()
            .filter(result -> !result.isSuccess())
            .count();
    }

    /**
     * 验证节点配置。
     *
     * @param node 工作流节点
     */
    @Override
    protected void validateNodeConfig(DagWorkflowNode node) {
        // 结束节点不需要强制配置
    }
}
