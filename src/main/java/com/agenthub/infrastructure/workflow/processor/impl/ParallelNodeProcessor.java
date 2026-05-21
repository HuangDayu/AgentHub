package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.*;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.processor.NodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 并行节点处理器.
 */
@Slf4j
@Component
public class ParallelNodeProcessor extends AbstractNodeProcessor {

    private final Map<NodeType, NodeProcessor> processorMap;

    /**
     * 构造函数.
     */
    public ParallelNodeProcessor(Map<NodeType, NodeProcessor> processorMap) {
        this.processorMap = processorMap;
    }

    /**
     * 执行并行节点.
     */
    @Override
    protected Mono<Map<String, Object>> doProcess(WorkflowNode node, WorkflowContext context) {
        return Mono.fromCallable(() -> {
            NodeConfig config = node.getConfig();
            List<WorkflowNode> parallelNodes = getParallelNodes(config);
            int concurrency = getConcurrency(config);
            return executeParallel(parallelNodes, context, concurrency);
        });
    }

    /**
     * 获取并行节点列表.
     */
    @SuppressWarnings("unchecked")
    private List<WorkflowNode> getParallelNodes(NodeConfig config) {
        List<Map<String, Object>> nodesConfig = (List<Map<String, Object>>) config.getParameters().get("nodes");
        if (nodesConfig == null) return new ArrayList<>();
        return nodesConfig.stream()
            .map(this::buildParallelNode)
            .toList();
    }

    /**
     * 构建并行节点.
     */
    @SuppressWarnings("unchecked")
    private WorkflowNode buildParallelNode(Map<String, Object> nodeConfig) {
        WorkflowNode node = WorkflowNode.create(
            NodeType.valueOf((String) nodeConfig.get("type")),
            (String) nodeConfig.get("name")
        );
        node.setConfig(buildNodeConfig((Map<String, Object>) nodeConfig.get("config")));
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
     * 获取并发度.
     */
    private int getConcurrency(NodeConfig config) {
        Object concurrencyValue = config.getParameters().getOrDefault("concurrency", 4);
        if (concurrencyValue instanceof Number) {
            return ((Number) concurrencyValue).intValue();
        }
        return 4;
    }

    /**
     * 并行执行节点.
     */
    private Map<String, Object> executeParallel(List<WorkflowNode> nodes, 
                                                 WorkflowContext context, int concurrency) {
        List<Map<String, Object>> results = Flux.fromIterable(nodes)
            .flatMap(node -> executeNodeAsync(node, context), concurrency)
            .collectList()
            .block();

        Map<String, Object> output = new HashMap<>();
        output.put("results", results);
        output.put("totalNodes", nodes.size());
        return output;
    }

    /**
     * 异步执行单个节点.
     */
    private Mono<Map<String, Object>> executeNodeAsync(WorkflowNode node, WorkflowContext context) {
        return Mono.fromCallable(() -> {
            NodeProcessor processor = processorMap.get(node.getType());
            NodeResult result = processor.process(node, context).block();
            return result != null ? result.getOutputs() : new HashMap<>();
        });
    }

    /**
     * 支持的节点类型.
     */
    @Override
    public String getSupportedType() {
        return NodeType.PARALLEL.name();
    }
}
