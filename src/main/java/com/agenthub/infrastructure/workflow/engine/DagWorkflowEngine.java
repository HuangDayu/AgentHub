package com.agenthub.infrastructure.workflow.engine;

import com.agenthub.domain.enums.workflow.DagNodeStatus;
import com.agenthub.domain.enums.workflow.DagWorkflowStatus;
import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.DagWorkflowContext;
import com.agenthub.domain.model.workflow.DagWorkflowGraph;
import com.agenthub.domain.model.workflow.DagWorkflowNode;
import com.agenthub.infrastructure.workflow.processor.NodeProcessor;
import com.agenthub.infrastructure.workflow.state.DagWorkflowStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流引擎。
 * 负责工作流的执行调度和节点处理。
 *
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DagWorkflowEngine {

    private final DagWorkflowBuilder dagBuilder;
    private final DagWorkflowStateManager stateManager;
    private final List<NodeProcessor> processors;
    private final Set<String> activeExecutions = ConcurrentHashMap.newKeySet();

    /**
     * 执行工作流（流式）。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    public Flux<NodeResult> execute(DagWorkflowContext context) {
        return validateContext(context)
            .flatMapMany(ctx -> buildDagAndExecute(ctx));
    }

    /**
     * 验证执行上下文。
     *
     * @param context 执行上下文
     * @return 验证后的上下文
     */
    private Mono<DagWorkflowContext> validateContext(DagWorkflowContext context) {
        return Mono.just(context)
            .doOnNext(ctx -> activeExecutions.add(ctx.getExecutionId()));
    }

    /**
     * 构建DAG并执行。
     *
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    private Flux<NodeResult> buildDagAndExecute(DagWorkflowContext context) {
        return Mono.fromSupplier(() -> dagBuilder.build(context.getGraph()))
            .flatMapMany(result -> executeDag(result, context));
    }

    /**
     * 执行DAG。
     *
     * @param result DAG构建结果
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    private Flux<NodeResult> executeDag(DagWorkflowBuilder.DagBuildResult result, DagWorkflowContext context) {
        if (!result.isSuccess()) {
            return Flux.error(new IllegalStateException("DAG构建失败"));
        }
        return executeNodes(result.getDag(), result.getGraph(), context);
    }

    /**
     * 执行所有节点。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     * @param context 执行上下文
     * @return 节点执行结果流
     */
    private Flux<NodeResult> executeNodes(
            DirectedAcyclicGraph<String, DefaultEdge> dag,
            DagWorkflowGraph graph,
            DagWorkflowContext context) {
        Set<String> completedNodes = new HashSet<>();
        return executeInTopologicalOrder(dag, graph, context, completedNodes);
    }

    /**
     * 按拓扑顺序执行节点。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     * @param context 执行上下文
     * @param completedNodes 已完成节点
     * @return 节点执行结果流
     */
    private Flux<NodeResult> executeInTopologicalOrder(
            DirectedAcyclicGraph<String, DefaultEdge> dag,
            DagWorkflowGraph graph,
            DagWorkflowContext context,
            Set<String> completedNodes) {
        List<String> topologicalOrder = dagBuilder.topologicalSort(dag);
        return Flux.fromIterable(topologicalOrder)
            .concatMap(nodeId -> executeNode(nodeId, graph, context, completedNodes));
    }

    /**
     * 执行单个节点。
     *
     * @param nodeId 节点ID
     * @param graph 工作流图
     * @param context 执行上下文
     * @param completedNodes 已完成节点
     * @return 节点执行结果
     */
    private Mono<NodeResult> executeNode(
            String nodeId,
            DagWorkflowGraph graph,
            DagWorkflowContext context,
            Set<String> completedNodes) {
        DagWorkflowNode node = graph.findNode(nodeId);
        if (node == null) {
            return Mono.just(NodeResult.failure(nodeId, "节点不存在"));
        }
        return processNode(node, context)
            .doOnNext(result -> handleNodeCompletion(result, completedNodes, context));
    }

    /**
     * 处理节点执行。
     *
     * @param node 工作流节点
     * @param context 执行上下文
     * @return 节点执行结果
     */
    private Mono<NodeResult> processNode(DagWorkflowNode node, DagWorkflowContext context) {
        NodeProcessor processor = findProcessor(node);
        if (processor == null) {
            return Mono.just(NodeResult.failure(node.getId(), "未找到处理器"));
        }
        return processor.process(node, context);
    }

    /**
     * 查找节点处理器。
     *
     * @param node 工作流节点
     * @return 节点处理器
     */
    private NodeProcessor findProcessor(DagWorkflowNode node) {
        return processors.stream()
            .filter(p -> p.supports(node))
            .findFirst()
            .orElse(null);
    }

    /**
     * 处理节点完成。
     *
     * @param result 节点结果
     * @param completedNodes 已完成节点
     * @param context 执行上下文
     */
    private void handleNodeCompletion(
            NodeResult result,
            Set<String> completedNodes,
            DagWorkflowContext context) {
        completedNodes.add(result.getNodeId());
        context.recordNodeResult(result);
    }

    /**
     * 停止执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    public Mono<Void> stop(String executionId) {
        return Mono.fromRunnable(() -> activeExecutions.remove(executionId))
            .then(stateManager.updateStatus(executionId, DagWorkflowStatus.CANCELLED))
            .then();
    }

    /**
     * 暂停执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    public Mono<Void> pause(String executionId) {
        return stateManager.updateStatus(executionId, DagWorkflowStatus.PAUSED);
    }

    /**
     * 恢复执行。
     *
     * @param executionId 执行ID
     * @return 执行上下文
     */
    public Mono<DagWorkflowContext> resume(String executionId) {
        return stateManager.loadContext(executionId)
            .flatMap(context -> stateManager.updateStatus(executionId, DagWorkflowStatus.EXECUTING)
                .thenReturn(context));
    }
}
