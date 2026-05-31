package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.enums.workflow.DagNodeType;
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
    private final Map<DagNodeType, NodeProcessor> processorMap;

    /**
     * 构造函数.
     */
    public LoopNodeProcessor(VariableResolver variableResolver, Map<DagNodeType, NodeProcessor> processorMap) {
        this.variableResolver = variableResolver;
        this.processorMap = processorMap;
    }

    /**
     * 执行循环节点.
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(DagWorkflowNode node, DagWorkflowContext context) {
        return Mono.fromCallable(() -> {
            NodeConfig config = node.getConfig();
            List<?> items = getLoopItems(config, context);
            DagWorkflowNode loopBody = getLoopBody(config);
            return executeLoop(items, loopBody, context, node);
        });
    }

    /**
     * 获取循环项列表.
     */
    private List<?> getLoopItems(NodeConfig config, DagWorkflowContext context) {
        String itemsExpression = (String) config.getParameters().get("items");
        Object items = variableResolver.resolve(itemsExpression, context);
        return items instanceof List ? (List<?>) items : new ArrayList<>();
    }

    /**
     * 获取循环体节点.
     */
    @SuppressWarnings("unchecked")
    private DagWorkflowNode getLoopBody(NodeConfig config) {
        Map<String, Object> bodyConfig = (Map<String, Object>) config.getParameters().get("body");
        
        // 如果没有配置 body，创建一个空操作的循环体
        if (bodyConfig == null || bodyConfig.isEmpty()) {
            log.warn("循环节点未配置 body，创建空操作循环体");
            return createNoOpLoopBody();
        }
        
        return buildLoopBodyNode(bodyConfig);
    }
    
    /**
     * 创建空操作的循环体节点.
     */
    private DagWorkflowNode createNoOpLoopBody() {
        DagWorkflowNode node = DagWorkflowNode.create(DagNodeType.CODE, "EmptyLoopBody");
        Map<String, Object> params = new HashMap<>();
        params.put("script", "{}; // No-op");
        node.setConfig(new NodeConfig(params, 5000L, 0));
        return node;
    }

    /**
     * 构建循环体节点.
     */
    @SuppressWarnings("unchecked")
    private DagWorkflowNode buildLoopBodyNode(Map<String, Object> bodyConfig) {
        DagWorkflowNode node = DagWorkflowNode.create(
            DagNodeType.valueOf((String) bodyConfig.get("type")),
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
    private Map<String, Object> executeLoop(List<?> items, DagWorkflowNode loopBody, 
                                            DagWorkflowContext context, DagWorkflowNode node) {
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
        Object maxIterationsValue = config.getParameters().getOrDefault("maxIterations", 100);
        if (maxIterationsValue instanceof Number) {
            return ((Number) maxIterationsValue).intValue();
        }
        return 100;
    }

    /**
     * 执行单次迭代.
     */
    private Map<String, Object> executeIteration(Object item, DagWorkflowNode loopBody, 
                                                  DagWorkflowContext context, int index) {
        DagWorkflowContext iterationContext = createIterationContext(context, item, index);
        NodeProcessor processor = processorMap.get(loopBody.getType());
        NodeResult result = processor.process(loopBody, iterationContext).block();
        return result != null ? result.getOutputs() : new HashMap<>();
    }

    /**
     * 创建迭代上下文.
     */
    private DagWorkflowContext createIterationContext(DagWorkflowContext context, Object item, int index) {
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
        return DagNodeType.LOOP.name();
    }
}
