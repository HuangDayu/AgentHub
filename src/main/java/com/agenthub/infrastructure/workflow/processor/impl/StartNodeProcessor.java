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
 * 开始节点处理器。
 * 处理工作流的开始节点，初始化执行上下文。
 *
 * @author huangdayu
 */
@Component
public class StartNodeProcessor extends AbstractNodeProcessor {

    /**
     * 获取支持的节点类型。
     *
     * @return START类型
     */
    @Override
    public String getSupportedType() {
        return DagNodeType.START.name();
    }

    /**
     * 执行开始节点处理。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 初始化输出数据的Mono
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(
            DagWorkflowNode node, DagWorkflowContext context) {
        return Mono.fromSupplier(() -> initializeOutputs(node, context));
    }

    /**
     * 初始化输出数据。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 初始化的输出数据
     */
    private Map<String, Object> initializeOutputs(DagWorkflowNode node, 
                                                   DagWorkflowContext context) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("started", true);
        outputs.put("startTime", System.currentTimeMillis());
        addInitialVariables(outputs, node);
        return outputs;
    }

    /**
     * 添加初始变量到输出。
     *
     * @param outputs 输出数据
     * @param node 工作流节点
     */
    private void addInitialVariables(Map<String, Object> outputs, DagWorkflowNode node) {
        if (node.getConfig() != null && node.getConfig().getParameters() != null) {
            outputs.putAll(node.getConfig().getParameters());
        }
    }

    /**
     * 验证节点配置。
     *
     * @param node 工作流节点
     */
    @Override
    protected void validateNodeConfig(DagWorkflowNode node) {
        // 开始节点不需要强制配置
    }
}
