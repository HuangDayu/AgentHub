package com.agenthub.infrastructure.workflow.adapter;

import com.agenthub.application.command.workflow.ExecutionCommand;
import com.agenthub.application.port.out.repositories.WorkflowRepository;
import com.agenthub.application.port.out.workflow.WorkflowExecutionPort;
import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.enums.workflow.WorkflowStatus;
import com.agenthub.domain.model.workflow.NodeConfig;
import com.agenthub.domain.model.workflow.NodePosition;
import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.Workflow;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.WorkflowEdge;
import com.agenthub.domain.model.workflow.WorkflowGraph;
import com.agenthub.domain.model.workflow.WorkflowNode;
import com.agenthub.infrastructure.workflow.engine.WorkflowEngine;
import com.agenthub.infrastructure.workflow.state.WorkflowStateManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流执行端口适配器。
 * 实现WorkflowExecutionPort接口，调用WorkflowEngine执行工作流。
 *
 * @author huangdayu
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class WorkflowExecutionAdapter implements WorkflowExecutionPort {

    private final WorkflowEngine workflowEngine;
    private final WorkflowStateManager stateManager;
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;

    /**
     * 初始化执行上下文。
     *
     * @return 执行上下文
     */
    @Override
    public Mono<WorkflowContext> initializeContext(ExecutionCommand command) {
        return Mono.fromSupplier(() -> createContext(command))
            .flatMap(context -> stateManager.saveContext(context).thenReturn(context));
    }

    /**
     * 创建执行上下文。
     *
     * @return 执行上下文
     */
    private WorkflowContext createContext(ExecutionCommand command) {
        WorkflowContext context = WorkflowContext.create(command.getWorkflowId());
        context.setGraph(loadGraph(command.getWorkflowId()));
        context.setTenantId(command.getTenantId());
        context.setWorkspaceId(command.getWorkspaceId());
        context.setTriggeredBy(command.getTriggeredBy());
        if (command.getInput() != null) {
            command.getInput().forEach(context::setVariable);
        }
        return context;
    }

    /**
     * 从数据库加载工作流图。
     */
    private WorkflowGraph loadGraph(String workflowId) {
        return workflowRepository.findById(workflowId)
            .map(Workflow::getGraphDefinition)
            .filter(s -> s != null && !s.isBlank())
            .map(this::parseGraphDefinition)
            .orElseGet(() -> {
                log.warn("Workflow {} has no graph definition, using empty graph", workflowId);
                return WorkflowGraph.empty();
            });
    }

    /**
     * 将JsonNode转换为Java值对象。
     */
    private Object convertJsonNodeToValue(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            node.fieldNames().forEachRemaining(key -> {
                map.put(key, convertJsonNodeToValue(node.get(key)));
            });
            return map;
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            node.forEach(item -> list.add(convertJsonNodeToValue(item)));
            return list;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isInt() || node.isLong()) {
            return node.asLong();
        }
        if (node.isFloat() || node.isDouble()) {
            return node.asDouble();
        }
        if (node.isBigDecimal()) {
            return node.decimalValue();
        }
        return node.asText();
    }

    /**
     * 将前端的 graphDefinition JSON 解析为 WorkflowGraph。
     * 前端格式使用 Vue Flow 的 JSON 结构：
     * {"nodes":[{"id":"...","type":"start","position":{"x":50,"y":320},"data":{...}}],
     *  "edges":[{"id":"...","source":"...","target":"..."}]}
     */
    private WorkflowGraph parseGraphDefinition(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            List<WorkflowNode> nodes = new ArrayList<>();
            JsonNode nodesArray = root.get("nodes");
            if (nodesArray != null && nodesArray.isArray()) {
                for (JsonNode nodeJson : nodesArray) {
                    String id = nodeJson.get("id").asText();
                    String typeStr = nodeJson.get("type").asText();
                    NodeType type = NodeType.valueOf(typeStr.toUpperCase().replace("-", "_"));

                    WorkflowNode node = new WorkflowNode(id, type);

                    // 解析 data 字段
                    JsonNode dataJson = nodeJson.get("data");
                    if (dataJson != null) {
                        // 设置节点名称
                        JsonNode labelJson = dataJson.get("label");
                        if (labelJson != null) {
                            node.setName(labelJson.asText());
                        }

                        // 设置节点配置（从 node_param 提取）
                        JsonNode nodeParamJson = dataJson.get("node_param");
                        if (nodeParamJson != null && !nodeParamJson.isEmpty()) {
                            Map<String, Object> params = new HashMap<>();
                            nodeParamJson.fieldNames().forEachRemaining(key -> {
                                JsonNode value = nodeParamJson.get(key);
                                params.put(key, convertJsonNodeToValue(value));
                            });
                            node.setConfig(new NodeConfig(params, 30000, 0));
                        } else {
                            node.setConfig(NodeConfig.defaultConfig());
                        }
                    } else {
                        node.setConfig(NodeConfig.defaultConfig());
                    }

                    JsonNode posJson = nodeJson.get("position");
                    if (posJson != null && posJson.has("x") && posJson.has("y")) {
                        node.setPosition(new NodePosition(
                            posJson.get("x").asDouble(),
                            posJson.get("y").asDouble()));
                    }

                    nodes.add(node);
                }
            }

            List<WorkflowEdge> edges = new ArrayList<>();
            JsonNode edgesArray = root.get("edges");
            if (edgesArray != null && edgesArray.isArray()) {
                for (JsonNode edgeJson : edgesArray) {
                    String id = edgeJson.get("id").asText();
                    String source = edgeJson.get("source").asText();
                    String target = edgeJson.get("target").asText();
                    edges.add(new WorkflowEdge(id, source, target));
                }
            }

            WorkflowGraph graph = new WorkflowGraph(nodes, edges);
            log.debug("Parsed graph: {} nodes, {} edges", nodes.size(), edges.size());
            return graph;
        } catch (Exception e) {
            log.error("Failed to parse graph definition: {}", e.getMessage());
            return WorkflowGraph.empty();
        }
    }

    /**
     * 执行单个节点。
     *
     * @param context 执行上下文
     * @param nodeId 节点ID
     * @return 节点执行结果流
     */
    @Override
    public Flux<NodeResult> executeNode(WorkflowContext context, String nodeId) {
        return workflowEngine.execute(context)
            .filter(result -> result.getNodeId().equals(nodeId));
    }

    /**
     * 执行整个工作流。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    @Override
    public Flux<NodeResult> executeWorkflow(WorkflowContext context) {
        return executeWithLifecycle(context);
    }

    /**
     * 执行工作流（带生命周期管理）。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    private Flux<NodeResult> executeWithLifecycle(WorkflowContext context) {
        return Flux.defer(() -> startExecution(context))
            .concatMap(ctx -> runWorkflow(ctx))
            .doFinally(signal -> endExecution(context));
    }
    /**
     * 开始执行。
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    private Mono<WorkflowContext> startExecution(WorkflowContext context) {
        return stateManager.updateStatus(context.getExecutionId(), WorkflowStatus.EXECUTING)
            .thenReturn(context);
    }

    /**
     * 运行工作流。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    private Flux<NodeResult> runWorkflow(WorkflowContext context) {
        return workflowEngine.execute(context)
            .doOnNext(result -> saveNodeResult(context, result));
    }

    /**
     * 保存节点结果。
     *
     * @param context 执行上下文
     * @param result 节点结果
     */
    private void saveNodeResult(WorkflowContext context, NodeResult result) {
        stateManager.saveNodeResult(context.getExecutionId(), result).subscribe();
    }

    /**
     * 结束执行。
     *
     * @param context 执行上下文
     */
    private void endExecution(WorkflowContext context) {
        WorkflowStatus finalStatus = determineFinalStatus(context);
        stateManager.updateStatus(context.getExecutionId(), finalStatus).subscribe();
    }

    /**
     * 确定最终状态。
     *
     * @param context 执行上下文
     * @return 最终状态
     */
    private WorkflowStatus determineFinalStatus(WorkflowContext context) {
        boolean hasFailure = context.getNodeResults().values().stream()
            .anyMatch(result -> !result.isSuccess());
        return hasFailure ? WorkflowStatus.FAILED : WorkflowStatus.SUCCESS;
    }

    /**
     * 停止执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    @Override
    public Mono<Void> stopExecution(String executionId) {
        return workflowEngine.stop(executionId);
    }
}
