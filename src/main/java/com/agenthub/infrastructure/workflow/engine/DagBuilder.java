package com.agenthub.infrastructure.workflow.engine;

import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.WorkflowEdge;
import com.agenthub.domain.model.workflow.WorkflowGraph;
import com.agenthub.domain.model.workflow.WorkflowNode;
import lombok.Getter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAG构建器。
 * 负责构建和验证工作流的有向无环图。
 *
 * @author huangdayu
 */
@Component
public class DagBuilder {

    /**
     * 构建DAG图。
     *
     * @param graph 工作流图
     * @return DAG构建结果
     */
    public DagBuildResult build(WorkflowGraph graph) {
        DirectedAcyclicGraph<String, DefaultEdge> dag = createDag();
        addNodesToDag(dag, graph);
        addEdgesToDag(dag, graph);
        return validateAndCreateResult(dag, graph);
    }

    /**
     * 创建空的DAG实例。
     *
     * @return 空的DAG
     */
    private DirectedAcyclicGraph<String, DefaultEdge> createDag() {
        return new DirectedAcyclicGraph<>(DefaultEdge.class);
    }

    /**
     * 添加节点到DAG。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     */
    private void addNodesToDag(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                               WorkflowGraph graph) {
        graph.getNodes().forEach(node -> dag.addVertex(node.getId()));
    }

    /**
     * 添加边到DAG。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     */
    private void addEdgesToDag(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                               WorkflowGraph graph) {
        graph.getEdges().forEach(edge -> addEdgeSafe(dag, edge));
    }

    /**
     * 安全添加边到DAG。
     *
     * @param dag DAG实例
     * @param edge 工作流边
     */
    private void addEdgeSafe(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                             WorkflowEdge edge) {
        try {
            dag.addEdge(edge.getSourceNodeId(), edge.getTargetNodeId());
        } catch (IllegalArgumentException e) {
            // 边会导致环，将在验证阶段处理
        }
    }

    /**
     * 验证并创建构建结果。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     * @return 构建结果
     */
    private DagBuildResult validateAndCreateResult(
            DirectedAcyclicGraph<String, DefaultEdge> dag, 
            WorkflowGraph graph) {
        List<String> errors = validate(dag, graph);
        if (errors.isEmpty()) {
            return DagBuildResult.success(dag, graph);
        }
        return DagBuildResult.failure(errors);
    }

    /**
     * 验证DAG的合法性。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     * @return 验证错误列表
     */
    private List<String> validate(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                                  WorkflowGraph graph) {
        List<String> errors = new ArrayList<>();
        validateStartNode(graph, errors);
        validateEndNode(graph, errors);
        validateConnectivity(dag, graph, errors);
        return errors;
    }

    /**
     * 验证开始节点。
     *
     * @param graph 工作流图
     * @param errors 错误列表
     */
    private void validateStartNode(WorkflowGraph graph, List<String> errors) {
        long startCount = countNodesByType(graph, NodeType.START);
        if (startCount == 0) {
            errors.add("工作流缺少开始节点");
        } else if (startCount > 1) {
            errors.add("工作流只能有一个开始节点");
        }
    }

    /**
     * 验证结束节点。
     *
     * @param graph 工作流图
     * @param errors 错误列表
     */
    private void validateEndNode(WorkflowGraph graph, List<String> errors) {
        long endCount = countNodesByType(graph, NodeType.END);
        if (endCount == 0) {
            errors.add("工作流缺少结束节点");
        } else if (endCount > 1) {
            errors.add("工作流只能有一个结束节点");
        }
    }

    /**
     * 统计指定类型的节点数量。
     *
     * @param graph 工作流图
     * @param type 节点类型
     * @return 节点数量
     */
    private long countNodesByType(WorkflowGraph graph, NodeType type) {
        return graph.getNodes().stream()
            .filter(node -> node.getType() == type)
            .count();
    }

    /**
     * 验证图的连通性。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     * @param errors 错误列表
     */
    private void validateConnectivity(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                                      WorkflowGraph graph, 
                                      List<String> errors) {
        validateAllNodesConnected(dag, graph, errors);
        validateEdgeCount(dag, graph, errors);
        validateOrphanNodes(dag, graph, errors);
    }

    /**
     * 验证是否存在孤立节点（没有入边也没有出边的节点）。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     * @param errors 错误列表
     */
    private void validateOrphanNodes(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                                     WorkflowGraph graph, 
                                     List<String> errors) {
        Set<String> orphanNodes = graph.getNodes().stream()
            .map(WorkflowNode::getId)
            .filter(nodeId -> dag.incomingEdgesOf(nodeId).isEmpty() && dag.outgoingEdgesOf(nodeId).isEmpty())
            .collect(Collectors.toSet());
        
        if (!orphanNodes.isEmpty()) {
            errors.add("存在孤立节点: " + orphanNodes);
        }
    }

    /**
     * 验证所有节点都已连接。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     * @param errors 错误列表
     */
    private void validateAllNodesConnected(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                                           WorkflowGraph graph, 
                                           List<String> errors) {
        Set<String> unconnectedNodes = graph.getNodes().stream()
            .map(WorkflowNode::getId)
            .filter(nodeId -> !dag.containsVertex(nodeId))
            .collect(Collectors.toSet());
        
        if (!unconnectedNodes.isEmpty()) {
            errors.add("存在未连接的节点: " + unconnectedNodes);
        }
    }

    /**
     * 验证边数量一致性。
     *
     * @param dag DAG实例
     * @param graph 工作流图
     * @param errors 错误列表
     */
    private void validateEdgeCount(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                                   WorkflowGraph graph, 
                                   List<String> errors) {
        if (dag.edgeSet().size() != graph.getEdges().size()) {
            errors.add("图中存在环，请检查节点连接关系");
        }
    }

    /**
     * 获取拓扑排序。
     *
     * @param dag DAG实例
     * @return 拓扑排序列表
     */
    public List<String> topologicalSort(DirectedAcyclicGraph<String, DefaultEdge> dag) {
        List<String> order = new ArrayList<>();
        TopologicalOrderIterator<String, DefaultEdge> iterator =
            new TopologicalOrderIterator<>(dag);
        iterator.forEachRemaining(order::add);
        return order;
    }

    /**
     * 获取可并行执行的节点。
     *
     * @param dag DAG实例
     * @param completedNodes 已完成的节点
     * @return 可执行的节点集合
     */
    public Set<String> getExecutableNodes(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                                          Set<String> completedNodes) {
        return dag.vertexSet().stream()
            .filter(nodeId -> canExecute(dag, nodeId, completedNodes))
            .filter(nodeId -> !completedNodes.contains(nodeId))
            .collect(Collectors.toSet());
    }

    /**
     * 判断节点是否可执行。
     *
     * @param dag DAG实例
     * @param nodeId 节点ID
     * @param completedNodes 已完成的节点
     * @return 如果可执行返回true
     */
    private boolean canExecute(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                               String nodeId, 
                               Set<String> completedNodes) {
        Set<DefaultEdge> incomingEdges = dag.incomingEdgesOf(nodeId);
        return incomingEdges.stream()
            .allMatch(edge -> completedNodes.contains(dag.getEdgeSource(edge)));
    }

    /**
     * DAG构建结果。
     */
    @Getter
    public static class DagBuildResult {
        private final boolean success;
        private final DirectedAcyclicGraph<String, DefaultEdge> dag;
        private final WorkflowGraph graph;
        private final List<String> errors;

        private DagBuildResult(boolean success, 
                               DirectedAcyclicGraph<String, DefaultEdge> dag, 
                               WorkflowGraph graph, 
                               List<String> errors) {
            this.success = success;
            this.dag = dag;
            this.graph = graph;
            this.errors = errors;
        }

        /**
         * 创建成功的构建结果。
         *
         * @param dag DAG实例
         * @param graph 工作流图
         * @return 成功结果
         */
        public static DagBuildResult success(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                                             WorkflowGraph graph) {
            return new DagBuildResult(true, dag, graph, List.of());
        }

        /**
         * 创建失败的构建结果。
         *
         * @param errors 错误列表
         * @return 失败结果
         */
        public static DagBuildResult failure(List<String> errors) {
            return new DagBuildResult(false, null, null, errors);
        }
    }
}
