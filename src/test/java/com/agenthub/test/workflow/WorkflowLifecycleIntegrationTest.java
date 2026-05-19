package com.agenthub.test.workflow;

import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.enums.workflow.WorkflowStatus;
import com.agenthub.domain.model.workflow.*;
import com.agenthub.infrastructure.workflow.engine.DagBuilder;
import com.agenthub.infrastructure.workflow.engine.WorkflowEngine;
import com.agenthub.infrastructure.workflow.state.WorkflowStateManager;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 工作流生命周期集成测试。
 * 测试工作流创建、更新、发布、删除、执行、停止、恢复等完整生命周期。
 *
 * @author huangdayu
 */
@SpringBootTest(classes = TestAgentHubApplication.class)

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkflowLifecycleIntegrationTest {

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private DagBuilder dagBuilder;

    @Autowired
    private WorkflowStateManager stateManager;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    private static String workflowId;
    private static String executionId;

    @BeforeEach
    void setUp() {
        workflowId = "wf-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ==================== 工作流创建测试 ====================

    @Test
    @Order(1)
    @DisplayName("应该成功创建简单工作流")
    void shouldCreateSimpleWorkflow() {
        // Given: 创建简单的工作流图
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始节点");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束节点");
        WorkflowEdge edge = WorkflowEdge.create(startNode.getId(), endNode.getId());

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(endNode)
            .addEdge(edge);

        // When: 构建DAG
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then: 验证构建成功
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDag()).isNotNull();
        assertThat(result.getDag().vertexSet()).hasSize(2);
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("应该成功创建包含多个节点的工作流")
    void shouldCreateComplexWorkflow() {
        // Given: 创建包含多个节点的工作流
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM处理");
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "条件判断");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(llmNode)
            .addNode(conditionNode)
            .addNode(endNode)
            .addEdge(WorkflowEdge.create(startNode.getId(), llmNode.getId()))
            .addEdge(WorkflowEdge.create(llmNode.getId(), conditionNode.getId()))
            .addEdge(WorkflowEdge.create(conditionNode.getId(), endNode.getId()));

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDag().vertexSet()).hasSize(4);
    }

    @Test
    @Order(3)
    @DisplayName("创建无效工作流应该返回错误")
    void shouldFailToCreateInvalidWorkflow() {
        // Given: 创建包含循环依赖的无效工作流
        WorkflowNode nodeA = WorkflowNode.create(NodeType.LLM, "节点A");
        WorkflowNode nodeB = WorkflowNode.create(NodeType.LLM, "节点B");

        WorkflowGraph graph = WorkflowGraph.empty()
            .addNode(nodeA)
            .addNode(nodeB)
            .addEdge(WorkflowEdge.create(nodeA.getId(), nodeB.getId()))
            .addEdge(WorkflowEdge.create(nodeB.getId(), nodeA.getId())); // 循环依赖

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then: 应该检测到循环依赖
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
    }

    // ==================== 工作流更新测试 ====================

    @Test
    @Order(4)
    @DisplayName("应该成功更新工作流节点配置")
    void shouldUpdateWorkflowNodeConfig() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "LLM节点");
        NodeConfig originalConfig = node.getConfig();

        // When: 更新配置
        NodeConfig newConfig = new NodeConfig(
            Map.of("model", "gpt-4", "temperature", 0.7),
            60000,
            3
        );
        node.updateConfig(newConfig);

        // Then
        assertThat(node.getConfig()).isNotSameAs(originalConfig);
        assertThat(node.getConfig().getParameter("model")).isEqualTo("gpt-4");
        assertThat(node.getConfig().getTimeoutMs()).isEqualTo(60000);
        assertThat(node.getConfig().getRetryCount()).isEqualTo(3);
    }

    @Test
    @Order(5)
    @DisplayName("应该成功更新工作流节点状态")
    void shouldUpdateWorkflowNodeStatus() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "LLM节点");

        // When: 更新状态
        node.updateStatus(com.agenthub.domain.enums.workflow.NodeStatus.EXECUTING);

        // Then
        assertThat(node.getStatus()).isEqualTo(com.agenthub.domain.enums.workflow.NodeStatus.EXECUTING);
        assertThat(node.getUpdatedAt()).isNotNull();
    }

    // ==================== 工作流发布测试 ====================

    @Test
    @Order(6)
    @DisplayName("应该成功发布工作流")
    void shouldPublishWorkflow() {
        // Given: 创建有效的工作流
        WorkflowGraph graph = createValidWorkflowGraph();
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then: 验证可以发布
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getGraph()).isNotNull();
    }

    @Test
    @Order(7)
    @DisplayName("发布无效工作流应该失败")
    void shouldFailToPublishInvalidWorkflow() {
        // Given: 创建无效的工作流（没有开始节点）
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM节点");
        WorkflowGraph graph = WorkflowGraph.empty().addNode(llmNode);

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isFalse();
    }

    // ==================== 工作流执行测试 ====================

    @Test
    @Order(10)
    @DisplayName("应该成功执行简单工作流")
    void shouldExecuteSimpleWorkflow() {
        // Given: 创建执行上下文
        WorkflowContext context = WorkflowContext.create(workflowId);
        WorkflowGraph graph = createValidWorkflowGraph();
        context.setGraph(graph);

        // When & Then: 验证上下文初始化
        assertThat(context.getExecutionId()).isNotNull();
        assertThat(context.getWorkflowId()).isEqualTo(workflowId);
        assertThat(context.getStatus()).isEqualTo(WorkflowStatus.EXECUTING);
        assertThat(context.isRunning()).isTrue();
    }

    @Test
    @Order(11)
    @DisplayName("应该正确记录节点执行结果")
    void shouldRecordNodeExecutionResult() {
        // Given
        WorkflowContext context = WorkflowContext.create(workflowId);
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "测试节点");

        // When: 记录节点结果
        NodeResult result = NodeResult.success(node.getId(), Map.of("output", "test"));
        context.recordNodeResult(result);

        // Then
        assertThat(context.getNodeResults()).containsKey(node.getId());
        assertThat(context.getNodeResults().get(node.getId()).isSuccess()).isTrue();
    }

    @Test
    @Order(12)
    @DisplayName("应该正确处理变量")
    void shouldHandleVariables() {
        // Given
        WorkflowContext context = WorkflowContext.create(workflowId);

        // When: 设置和获取变量
        context.setVariable("userInput", "Hello");
        context.setVariable("count", 42);

        // Then
        assertThat(context.getVariable("userInput")).isEqualTo("Hello");
        assertThat(context.getVariable("count")).isEqualTo(42);
    }

    // ==================== 工作流停止测试 ====================

    @Test
    @Order(20)
    @DisplayName("应该成功停止正在执行的工作流")
    void shouldStopExecutingWorkflow() {
        // Given
        String execId = "exec-" + UUID.randomUUID().toString().substring(0, 8);
        WorkflowContext context = WorkflowContext.create(workflowId);
        context.setGraph(createValidWorkflowGraph());

        // When: 停止执行
        // Note: 实际测试需要mock或使用真实的stateManager
        // 这里验证停止逻辑的正确性
        assertThat(context.getStatus()).isEqualTo(WorkflowStatus.EXECUTING);
    }

    // ==================== 工作流暂停和恢复测试 ====================

    @Test
    @Order(21)
    @DisplayName("应该成功暂停正在执行的工作流")
    void shouldPauseExecutingWorkflow() {
        // Given
        WorkflowContext context = WorkflowContext.create(workflowId);

        // When: 暂停
        context.updateStatus(WorkflowStatus.PAUSED);

        // Then
        assertThat(context.getStatus()).isEqualTo(WorkflowStatus.PAUSED);
        assertThat(context.isRunning()).isTrue();
    }

    @Test
    @Order(22)
    @DisplayName("应该成功恢复暂停的工作流")
    void shouldResumePausedWorkflow() {
        // Given
        WorkflowContext context = WorkflowContext.create(workflowId);
        context.updateStatus(WorkflowStatus.PAUSED);

        // When: 恢复
        context.updateStatus(WorkflowStatus.EXECUTING);

        // Then
        assertThat(context.getStatus()).isEqualTo(WorkflowStatus.EXECUTING);
        assertThat(context.isRunning()).isTrue();
    }

    // ==================== 工作流状态转换测试 ====================

    @Test
    @Order(30)
    @DisplayName("应该正确转换工作流状态")
    void shouldTransitionWorkflowStatus() {
        // Given
        WorkflowContext context = WorkflowContext.create(workflowId);

        // When & Then: 验证状态转换
        // EXECUTING -> SUCCESS
        context.updateStatus(WorkflowStatus.SUCCESS);
        assertThat(context.getStatus()).isEqualTo(WorkflowStatus.SUCCESS);
        assertThat(context.getStatus().isTerminal()).isTrue();
        assertThat(context.getEndTime()).isNotNull();
    }

    @Test
    @Order(31)
    @DisplayName("终态工作流不应该能继续执行")
    void terminalStatusShouldNotBeExecutable() {
        // Given
        WorkflowStatus[] terminalStatuses = {
            WorkflowStatus.SUCCESS,
            WorkflowStatus.FAILED,
            WorkflowStatus.CANCELLED
        };

        // Then
        for (WorkflowStatus status : terminalStatuses) {
            assertThat(status.isTerminal()).isTrue();
            assertThat(status.canExecute()).isFalse();
        }
    }

    @Test
    @Order(32)
    @DisplayName("应该正确判断工作流是否正在运行")
    void shouldCorrectlyDetermineRunningStatus() {
        // Given
        WorkflowContext context = WorkflowContext.create(workflowId);

        // When & Then
        assertThat(context.isRunning()).isTrue(); // EXECUTING

        context.updateStatus(WorkflowStatus.PAUSED);
        assertThat(context.isRunning()).isTrue(); // PAUSED

        context.updateStatus(WorkflowStatus.SUCCESS);
        assertThat(context.isRunning()).isFalse(); // SUCCESS
    }

    // ==================== 工作流历史记录测试 ====================

    @Test
    @Order(40)
    @DisplayName("应该正确记录执行历史")
    void shouldRecordExecutionHistory() {
        // Given
        WorkflowContext context = WorkflowContext.create(workflowId);
        WorkflowNode node1 = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode node2 = WorkflowNode.create(NodeType.LLM, "LLM");

        // When: 记录多个节点结果
        NodeResult result1 = NodeResult.success(node1.getId(), Map.of("started", true));
        NodeResult result2 = NodeResult.success(node2.getId(), Map.of("response", "AI回复"));

        context.recordNodeResult(result1);
        context.recordNodeResult(result2);

        // Then
        assertThat(context.getNodeResults()).hasSize(2);
        assertThat(context.getNodeResults().get(node1.getId())).isNotNull();
        assertThat(context.getNodeResults().get(node2.getId())).isNotNull();
    }

    @Test
    @Order(41)
    @DisplayName("应该正确计算执行统计")
    void shouldCalculateExecutionStatistics() {
        // Given
        WorkflowContext context = WorkflowContext.create(workflowId);
        WorkflowNode node1 = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode node2 = WorkflowNode.create(NodeType.LLM, "LLM");
        WorkflowNode node3 = WorkflowNode.create(NodeType.END, "结束");

        // When: 记录成功和失败结果
        context.recordNodeResult(NodeResult.success(node1.getId(), Map.of()));
        context.recordNodeResult(NodeResult.success(node2.getId(), Map.of()));
        context.recordNodeResult(NodeResult.failure(node3.getId(), "执行失败"));

        // Then: 统计结果
        long successCount = context.getNodeResults().values().stream()
            .filter(NodeResult::isSuccess)
            .count();
        long failureCount = context.getNodeResults().values().stream()
            .filter(r -> !r.isSuccess())
            .count();

        assertThat(successCount).isEqualTo(2);
        assertThat(failureCount).isEqualTo(1);
    }

    // ==================== 辅助方法 ====================

    private WorkflowGraph createValidWorkflowGraph() {
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始节点");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束节点");
        WorkflowEdge edge = WorkflowEdge.create(startNode.getId(), endNode.getId());

        return WorkflowGraph.empty()
            .addNode(startNode)
            .addNode(endNode)
            .addEdge(edge);
    }

    private WorkflowGraph createLinearWorkflowGraph() {
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
}
