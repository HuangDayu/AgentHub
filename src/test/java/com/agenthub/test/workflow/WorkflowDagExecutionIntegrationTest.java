package com.agenthub.test.workflow;

import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.enums.workflow.WorkflowStatus;
import com.agenthub.domain.model.workflow.*;
import com.agenthub.infrastructure.workflow.engine.DagBuilder;
import com.agenthub.infrastructure.workflow.engine.WorkflowEngine;
import com.agenthub.test.TestAgentHubApplication;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DAG执行集成测试。
 * 测试各种工作流拓扑结构的执行。
 *
 * @author huangdayu
 */
@SpringBootTest(classes = TestAgentHubApplication.class)

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkflowDagExecutionIntegrationTest {

    @Autowired
    private DagBuilder dagBuilder;

    @Autowired
    private WorkflowEngine workflowEngine;

    // ==================== 简单线性工作流测试 ====================

    @Test
    @Order(1)
    @DisplayName("应该成功执行简单线性工作流")
    void shouldExecuteSimpleLinearWorkflow() {
        // Given: 创建线性工作流 Start -> LLM -> End
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM处理");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(llmNode)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNode.getId()))
            .addEdge(WorkflowEdge.create(llmNode.getId(), endNode.getId()));

        // When: 构建DAG
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then: 验证DAG结构
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDag().vertexSet()).hasSize(3);
        assertThat(result.getDag().edgeSet()).hasSize(2);
    }

    @Test
    @Order(2)
    @DisplayName("线性工作流应该有正确的执行顺序")
    void linearWorkflowShouldHaveCorrectExecutionOrder() {
        // Given
        WorkflowGraph graph = createLinearWorkflow();
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // When: 获取拓扑排序
        List<String> topologicalOrder = getTopologicalOrder(result.getDag());

        // Then: 验证执行顺序
        assertThat(topologicalOrder).hasSize(3);
        // Start节点应该在最前面
        assertThat(topologicalOrder.get(0)).isEqualTo(findNodeIdByType(graph, NodeType.START));
        // End节点应该在最后面
        assertThat(topologicalOrder.get(2)).isEqualTo(findNodeIdByType(graph, NodeType.END));
    }

    // ==================== 条件分支工作流测试 ====================

    @Test
    @Order(10)
    @DisplayName("应该成功执行条件分支工作流")
    void shouldExecuteConditionalWorkflow() {
        // Given: 创建条件分支工作流
        //        ┌-> LLM_A ->
        // Start -> Condition -> End
        //        └-> LLM_B ->
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "条件判断");
        WorkflowNode llmNodeA = WorkflowNode.create(NodeType.LLM, "分支A");
        WorkflowNode llmNodeB = WorkflowNode.create(NodeType.LLM, "分支B");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(conditionNode)
            .addNode(llmNodeA)
            .addNode(llmNodeB)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), conditionNode.getId()))
            .addEdge(WorkflowEdge.createWithCondition(conditionNode.getId(), llmNodeA.getId(), "score > 60"))
            .addEdge(WorkflowEdge.createWithCondition(conditionNode.getId(), llmNodeB.getId(), "score <= 60"))
            .addEdge(WorkflowEdge.create(llmNodeA.getId(), endNode.getId()))
            .addEdge(WorkflowEdge.create(llmNodeB.getId(), endNode.getId()));

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDag().vertexSet()).hasSize(5);
    }

    @Test
    @Order(11)
    @DisplayName("条件分支工作流应该正确识别分支边")
    void conditionalWorkflowShouldIdentifyBranchEdges() {
        // Given
        WorkflowGraph graph = createConditionalWorkflow();

        // When: 获取条件节点的出边
        String conditionNodeId = findNodeIdByType(graph, NodeType.CONDITION);
        List<WorkflowEdge> outgoingEdges = graph.getOutgoingEdges(conditionNodeId);

        // Then: 验证有条件边
        assertThat(outgoingEdges).hasSize(2);
        assertThat(outgoingEdges.stream().filter(e -> !e.isUnconditional()).count()).isEqualTo(2);
    }

    // ==================== 并行工作流测试 ====================

    @Test
    @Order(20)
    @DisplayName("应该成功执行并行工作流")
    void shouldExecuteParallelWorkflow() {
        // Given: 创建并行工作流
        //        ┌-> LLM_A ┐
        // Start -> LLM_B -> End
        //        └-> LLM_C ┘
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode llmNodeA = WorkflowNode.create(NodeType.LLM, "并行任务A");
        WorkflowNode llmNodeB = WorkflowNode.create(NodeType.LLM, "并行任务B");
        WorkflowNode llmNodeC = WorkflowNode.create(NodeType.LLM, "并行任务C");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(llmNodeA)
            .addNode(llmNodeB)
            .addNode(llmNodeC)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNodeA.getId()))
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNodeB.getId()))
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNodeC.getId()))
            .addEdge(WorkflowEdge.create(llmNodeA.getId(), endNode.getId()))
            .addEdge(WorkflowEdge.create(llmNodeB.getId(), endNode.getId()))
            .addEdge(WorkflowEdge.create(llmNodeC.getId(), endNode.getId()));

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDag().vertexSet()).hasSize(5);
    }

    @Test
    @Order(21)
    @DisplayName("并行工作流应该识别可并行执行的节点")
    void parallelWorkflowShouldIdentifyParallelNodes() {
        // Given
        WorkflowGraph graph = createParallelWorkflow();
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);
        DirectedAcyclicGraph<String, DefaultEdge> dag = result.getDag();

        // When: 获取开始节点后的可执行节点
        String startNodeId = findNodeIdByType(graph, NodeType.START);
        Set<String> executableNodes = dagBuilder.getExecutableNodes(dag, Set.of(startNodeId));

        // Then: 应该有3个可并行执行的节点
        assertThat(executableNodes).hasSize(3);
    }

    @Test
    @Order(22)
    @DisplayName("并行工作流应该正确处理汇聚节点")
    void parallelWorkflowShouldHandleJoinNode() {
        // Given
        WorkflowGraph graph = createParallelWorkflow();
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);
        DirectedAcyclicGraph<String, DefaultEdge> dag = result.getDag();

        // When: 获取End节点的入边
        String endNodeId = findNodeIdByType(graph, NodeType.END);
        Set<DefaultEdge> incomingEdges = dag.incomingEdgesOf(endNodeId);

        // Then: End节点应该有3个入边（汇聚点）
        assertThat(incomingEdges).hasSize(3);
    }

    // ==================== 循环工作流测试 ====================

    @Test
    @Order(30)
    @DisplayName("应该成功执行包含循环节点的工作流")
    void shouldExecuteLoopWorkflow() {
        // Given: 创建包含循环节点的工作流
        // Start -> Loop -> End
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode loopNode = WorkflowNode.create(NodeType.LOOP, "循环节点");
        loopNode.updateConfig(new NodeConfig(Map.of(
            "items", List.of("item1", "item2", "item3"),
            "maxIterations", 10
        ), 30000, 0));
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(loopNode)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), loopNode.getId()))
            .addEdge(WorkflowEdge.create(loopNode.getId(), endNode.getId()));

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDag().vertexSet()).hasSize(3);
    }

    @Test
    @Order(31)
    @DisplayName("循环节点应该有正确的配置")
    void loopNodeShouldHaveCorrectConfig() {
        // Given
        WorkflowNode loopNode = WorkflowNode.create(NodeType.LOOP, "循环节点");
        loopNode.updateConfig(new NodeConfig(Map.of(
            "items", List.of(1, 2, 3, 4, 5),
            "maxIterations", 100
        ), 60000, 3));

        // When & Then
        assertThat(loopNode.getConfig().getParameter("items")).isNotNull();
        assertThat(loopNode.getConfig().getTimeoutMs()).isEqualTo(60000);
        assertThat(loopNode.getConfig().getRetryCount()).isEqualTo(3);
    }

    // ==================== 复杂嵌套工作流测试 ====================

    @Test
    @Order(40)
    @DisplayName("应该成功执行复杂嵌套工作流")
    void shouldExecuteComplexNestedWorkflow() {
        // Given: 创建复杂嵌套工作流
        // Start -> Condition -> Loop -> Parallel -> End
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "条件判断");
        WorkflowNode loopNode = WorkflowNode.create(NodeType.LOOP, "循环处理");
        WorkflowNode parallelNode = WorkflowNode.create(NodeType.PARALLEL, "并行处理");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(conditionNode)
            .addNode(loopNode)
            .addNode(parallelNode)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), conditionNode.getId()))
            .addEdge(WorkflowEdge.create(conditionNode.getId(), loopNode.getId()))
            .addEdge(WorkflowEdge.create(loopNode.getId(), parallelNode.getId()))
            .addEdge(WorkflowEdge.create(parallelNode.getId(), endNode.getId()));

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDag().vertexSet()).hasSize(5);
    }

    @Test
    @Order(41)
    @DisplayName("复杂工作流应该正确计算执行层级")
    void complexWorkflowShouldCalculateExecutionLevels() {
        // Given
        WorkflowGraph graph = createComplexWorkflow();
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);
        DirectedAcyclicGraph<String, DefaultEdge> dag = result.getDag();

        // When: 计算每个节点的层级
        Map<String, Integer> levels = calculateNodeLevels(dag);

        // Then: 验证层级关系
        String startNodeId = findNodeIdByType(graph, NodeType.START);
        String endNodeId = findNodeIdByType(graph, NodeType.END);
        
        assertThat(levels.get(startNodeId)).isEqualTo(0);
        assertThat(levels.get(endNodeId)).isGreaterThan(levels.get(startNodeId));
    }

    // ==================== DAG验证测试 ====================

    @Test
    @Order(50)
    @DisplayName("应该检测到循环依赖")
    void shouldDetectCyclicDependency() {
        // Given: 创建有循环依赖的工作流
        WorkflowNode nodeA = WorkflowNode.create(NodeType.LLM, "节点A");
        WorkflowNode nodeB = WorkflowNode.create(NodeType.LLM, "节点B");
        WorkflowNode nodeC = WorkflowNode.create(NodeType.LLM, "节点C");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(nodeA)
            .addNode(nodeB)
            .addNode(nodeC)
            .addEdge(WorkflowEdge.create(nodeA.getId(), nodeB.getId()))
            .addEdge(WorkflowEdge.create(nodeB.getId(), nodeC.getId()))
            .addEdge(WorkflowEdge.create(nodeC.getId(), nodeA.getId())); // 循环

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    @Order(51)
    @DisplayName("应该检测到孤立节点")
    void shouldDetectOrphanNodes() {
        // Given: 创建包含孤立节点的工作流
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");
        WorkflowNode orphanNode = WorkflowNode.create(NodeType.LLM, "孤立节点");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(endNode)
            .addNode(orphanNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), endNode.getId()));
            // orphanNode 没有任何连接

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then: 应该检测到孤立节点
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @Order(52)
    @DisplayName("应该验证必须有Start节点")
    void shouldValidateStartNodeRequired() {
        // Given: 创建没有Start节点的工作流
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(llmNode)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(llmNode.getId(), endNode.getId()));

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @Order(53)
    @DisplayName("应该验证必须有End节点")
    void shouldValidateEndNodeRequired() {
        // Given: 创建没有End节点的工作流
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(llmNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNode.getId()));

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isFalse();
    }

    // ==================== 辅助方法 ====================

    private WorkflowGraph createLinearWorkflow() {
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM处理");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        return WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(llmNode)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNode.getId()))
            .addEdge(WorkflowEdge.create(llmNode.getId(), endNode.getId()));
    }

    private WorkflowGraph createConditionalWorkflow() {
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "条件判断");
        WorkflowNode llmNodeA = WorkflowNode.create(NodeType.LLM, "分支A");
        WorkflowNode llmNodeB = WorkflowNode.create(NodeType.LLM, "分支B");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        return WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(conditionNode)
            .addNode(llmNodeA)
            .addNode(llmNodeB)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), conditionNode.getId()))
            .addEdge(WorkflowEdge.createWithCondition(conditionNode.getId(), llmNodeA.getId(), "score > 60"))
            .addEdge(WorkflowEdge.createWithCondition(conditionNode.getId(), llmNodeB.getId(), "score <= 60"))
            .addEdge(WorkflowEdge.create(llmNodeA.getId(), endNode.getId()))
            .addEdge(WorkflowEdge.create(llmNodeB.getId(), endNode.getId()));
    }

    private WorkflowGraph createParallelWorkflow() {
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode llmNodeA = WorkflowNode.create(NodeType.LLM, "并行任务A");
        WorkflowNode llmNodeB = WorkflowNode.create(NodeType.LLM, "并行任务B");
        WorkflowNode llmNodeC = WorkflowNode.create(NodeType.LLM, "并行任务C");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        return WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(llmNodeA)
            .addNode(llmNodeB)
            .addNode(llmNodeC)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNodeA.getId()))
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNodeB.getId()))
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNodeC.getId()))
            .addEdge(WorkflowEdge.create(llmNodeA.getId(), endNode.getId()))
            .addEdge(WorkflowEdge.create(llmNodeB.getId(), endNode.getId()))
            .addEdge(WorkflowEdge.create(llmNodeC.getId(), endNode.getId()));
    }

    private WorkflowGraph createComplexWorkflow() {
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "条件判断");
        WorkflowNode loopNode = WorkflowNode.create(NodeType.LOOP, "循环处理");
        WorkflowNode parallelNode = WorkflowNode.create(NodeType.PARALLEL, "并行处理");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        return WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(conditionNode)
            .addNode(loopNode)
            .addNode(parallelNode)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), conditionNode.getId()))
            .addEdge(WorkflowEdge.create(conditionNode.getId(), loopNode.getId()))
            .addEdge(WorkflowEdge.create(loopNode.getId(), parallelNode.getId()))
            .addEdge(WorkflowEdge.create(parallelNode.getId(), endNode.getId()));
    }

    private String findNodeIdByType(WorkflowGraph graph, NodeType type) {
        return graph.getNodes().stream()
            .filter(node -> node.getType() == type)
            .findFirst()
            .map(WorkflowNode::getId)
            .orElseThrow(() -> new IllegalArgumentException("Node type not found: " + type));
    }

    private List<String> getTopologicalOrder(DirectedAcyclicGraph<String, DefaultEdge> dag) {
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        for (String vertex : dag.vertexSet()) {
            if (!visited.contains(vertex)) {
                topologicalSort(dag, vertex, visited, result);
            }
        }
        
        Collections.reverse(result);
        return result;
    }

    private void topologicalSort(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                                 String vertex, Set<String> visited, List<String> result) {
        visited.add(vertex);
        
        for (DefaultEdge edge : dag.outgoingEdgesOf(vertex)) {
            String target = dag.getEdgeTarget(edge);
            if (!visited.contains(target)) {
                topologicalSort(dag, target, visited, result);
            }
        }
        
        result.add(vertex);
    }

    private Map<String, Integer> calculateNodeLevels(DirectedAcyclicGraph<String, DefaultEdge> dag) {
        Map<String, Integer> levels = new HashMap<>();
        
        for (String vertex : dag.vertexSet()) {
            int level = calculateLevel(dag, vertex, levels);
            levels.put(vertex, level);
        }
        
        return levels;
    }

    private int calculateLevel(DirectedAcyclicGraph<String, DefaultEdge> dag, 
                               String vertex, Map<String, Integer> cache) {
        if (cache.containsKey(vertex)) {
            return cache.get(vertex);
        }
        
        Set<DefaultEdge> incomingEdges = dag.incomingEdgesOf(vertex);
        if (incomingEdges.isEmpty()) {
            return 0;
        }
        
        int maxParentLevel = 0;
        for (DefaultEdge edge : incomingEdges) {
            String source = dag.getEdgeSource(edge);
            maxParentLevel = Math.max(maxParentLevel, calculateLevel(dag, source, cache));
        }
        
        return maxParentLevel + 1;
    }
}
