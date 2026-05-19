package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.*;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.processor.NodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 循环节点处理器.
 */
@Slf4j
@Component
public class LoopNodeProcessor extends AbstractNodeProcessor {

    private final VariableResolver variableResolver;
    private final Map<NodeType, NodeProcessor> processorMap;

    /**
     * 构造函数.
     */
    public LoopNodeProcessor(VariableResolver variableResolver, Map<NodeType, NodeProcessor> processorMap) {
        this.variableResolver = variableResolver;
        this.processorMap = processorMap;
    }

    /**
     * 执行循环节点.
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(WorkflowNode node, WorkflowContext context) {
        return Mono.fromCallable(() -> {
            NodeConfig config = node.getConfig();
            List<?> items = getLoopItems(config, context);
            WorkflowNode loopBody = getLoopBody(config);
            return executeLoop(items, loopBody, context, node);
        });
    }

    /**
     * 获取循环项列表.
     */
    private List<?> getLoopItems(NodeConfig config, WorkflowContext context) {
        String itemsExpression = (String) config.getParameters().get("items");
        Object items = variableResolver.resolve(itemsExpression, context);
        return items instanceof List ? (List<?>) items : new ArrayList<>();
    }

    /**
     * 获取循环体节点.
     */
    @SuppressWarnings("unchecked")
    private WorkflowNode getLoopBody(NodeConfig config) {
        Map<String, Object> bodyConfig = (Map<String, Object>) config.getParameters().get("body");
        return buildLoopBodyNode(bodyConfig);
    }

    /**
     * 构建循环体节点.
     */
    @SuppressWarnings("unchecked")
    private WorkflowNode buildLoopBodyNode(Map<String, Object> bodyConfig) {
        WorkflowNode node = WorkflowNode.create(
            NodeType.valueOf((String) bodyConfig.get("type")),
            (String) bodyConfig.get("name")
        );
        node.setConfig(buildNodeConfig((Map<String, Object>) bodyConfig.get("config")));
        return node;
    }

    /**
     * 构建节点配置.
     */
    @SuppressWarnings("unchecked")
    private NodeConfig buildNodeConfig(Map<String, Object> configMap) {
        if (configMap == null) return NodeConfig.defaultConfig();
        return new NodeConfig(
            (Map<String, Object>) configMap.getOrDefault("parameters", new HashMap<>()),
            (Long) configMap.getOrDefault("timeoutMs", 30000L),
            (Integer) configMap.getOrDefault("retryCount", 0)
        );
    }

    /**
     * 执行循环.
     */
    private Map<String, Object> executeLoop(List<?> items, WorkflowNode loopBody, 
                                            WorkflowContext context, WorkflowNode node) {
        List<Map<String, Object>> results = new ArrayList<>();
        int maxIterations = getMaxIterations(node.getConfig());
        int iterationCount = 0;

        for (Object item : items) {
            if (iterationCount >= maxIterations) break;
            Map<String, Object> result = executeIteration(item, loopBody, context, iterationCount);
            results.add(result);
            iterationCount++;
        }

        Map<String, Object> output = new HashMap<>();
        output.put("results", results);
        output.put("iterations", iterationCount);
        return output;
    }

    /**
     * 获取最大迭代次数.
     */
    private int getMaxIterations(NodeConfig config) {
        return (int) config.getParameters().getOrDefault("maxIterations", 100);
    }

    /**
     * 执行单次迭代.
     */
    private Map<String, Object> executeIteration(Object item, WorkflowNode loopBody, 
                                                  WorkflowContext context, int index) {
        WorkflowContext iterationContext = createIterationContext(context, item, index);
        NodeProcessor processor = processorMap.get(loopBody.getType());
        NodeResult result = processor.process(loopBody, iterationContext).block();
        return result != null ? result.getOutputs() : new HashMap<>();
    }

    /**
     * 创建迭代上下文.
     */
    private WorkflowContext createIterationContext(WorkflowContext context, Object item, int index) {
        Map<String, Object> loopVars = new HashMap<>();
        loopVars.put("item", item);
        loopVars.put("index", index);
        context.getVariables().put("loop", loopVars);
        return context;
    }

    /**
     * 支持的节点类型.
     */
    @Override
    public String getSupportedType() {
        return NodeType.LOOP.name();
    }
}
