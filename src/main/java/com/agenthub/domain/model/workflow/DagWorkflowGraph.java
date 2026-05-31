package com.agenthub.domain.model.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作流图值对象。
 * 表示整个工作流的DAG结构，包含所有节点和边。
 *
 * @author huangdayu
 */
@Data
public class DagWorkflowGraph {

    /** 所有节点 */
    private final List<DagWorkflowNode> nodes;

    /** 所有边 */
    private final List<DagWorkflowEdge> edges;

    @JsonCreator
    public DagWorkflowGraph(@JsonProperty("nodes") List<DagWorkflowNode> nodes,
                         @JsonProperty("edges") List<DagWorkflowEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    /** 图元数据 */
    private Map<String, Object> metadata;

    /**
     * 创建空的工作流图。
     *
     * @return 空图实例
     */
    public static DagWorkflowGraph empty() {
        return new DagWorkflowGraph(List.of(), List.of());
    }

    /**
     * 根据ID查找节点。
     *
     * @param nodeId 节点ID
     * @return 节点实例，不存在则返回null
     */
    public DagWorkflowNode findNode(String nodeId) {
        return nodes.stream()
            .filter(node -> node.getId().equals(nodeId))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取指定节点的所有入边。
     *
     * @param nodeId 节点ID
     * @return 入边列表
     */
    public List<DagWorkflowEdge> getIncomingEdges(String nodeId) {
        return edges.stream()
            .filter(edge -> edge.connectsTo(nodeId))
            .toList();
    }

    /**
     * 获取指定节点的所有出边。
     *
     * @param nodeId 节点ID
     * @return 出边列表
     */
    public List<DagWorkflowEdge> getOutgoingEdges(String nodeId) {
        return edges.stream()
            .filter(edge -> edge.getSourceNodeId().equals(nodeId))
            .toList();
    }

    /**
     * 获取所有节点ID集合。
     *
     * @return 节点ID集合
     */
    public Set<String> getNodeIds() {
        return nodes.stream()
            .map(DagWorkflowNode::getId)
            .collect(Collectors.toSet());
    }

    /**
     * 添加节点到图中。
     *
     * @param node 要添加的节点
     * @return 新的工作流图
     */
    public DagWorkflowGraph addNode(DagWorkflowNode node) {
        List<DagWorkflowNode> newNodes = new ArrayList<>(nodes);
        newNodes.add(node);
        return new DagWorkflowGraph(List.copyOf(newNodes), edges);
    }

    /**
     * 添加边到图中。
     *
     * @param edge 要添加的边
     * @return 新的工作流图
     */
    public DagWorkflowGraph addEdge(DagWorkflowEdge edge) {
        List<DagWorkflowEdge> newEdges = new ArrayList<>(edges);
        newEdges.add(edge);
        return new DagWorkflowGraph(nodes, List.copyOf(newEdges));
    }
}
