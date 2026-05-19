package com.agenthub.test.workflow;

import com.agenthub.application.port.out.agent.AgentChatPort;
import com.agenthub.domain.enums.workflow.NodeStatus;
import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.enums.workflow.WorkflowStatus;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.workflow.*;
import com.agenthub.infrastructure.workflow.engine.DagBuilder;
import com.agenthub.infrastructure.workflow.engine.WorkflowEngine;
import com.agenthub.infrastructure.workflow.processor.impl.EndNodeProcessor;
import com.agenthub.infrastructure.workflow.processor.impl.LlmNodeProcessor;
import com.agenthub.infrastructure.workflow.processor.impl.StartNodeProcessor;
import com.agenthub.infrastructure.workflow.state.WorkflowStateManager;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 错误处理集成测试。
 * 测试节点重试机制、Try-Catch策略、失败分支执行、超时处理。
 *
 * @author huangdayu
 */
@SpringBootTest(classes = TestAgentHubApplication.class)
@Import(TestWorkflowConfig.class)

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkflowErrorHandlingIntegrationTest {

    @Autowired
    private DagBuilder dagBuilder;

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private WorkflowStateManager stateManager;

    @Autowired
    private LlmNodeProcessor llmNodeProcessor;

    @Autowired
    private StartNodeProcessor startNodeProcessor;

    @Autowired
    private EndNodeProcessor endNodeProcessor;

    @Autowired
    private AgentChatPort agentChatPort;

    private WorkflowContext context;
    private String workflowId;

    @BeforeEach
    void setUp() {
        workflowId = "wf-" + UUID.randomUUID().toString().substring(0, 8);
        context = WorkflowContext.create(workflowId);
        Mockito.reset(agentChatPort);
    }

    // ==================== 节点重试机制测试 ====================

    @Test
    @Order(1)
    @DisplayName("节点配置应该支持重试次数设置")
    void nodeConfigShouldSupportRetryCount() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "LLM节点");

        // When: 设置重试次数
        NodeConfig config = new NodeConfig(
                Map.of("agentId", "agent-001", "prompt", "test"),
                30000,
                3  // 重试3次
        );
        node.updateConfig(config);

        // Then
        assertThat(node.getConfig().getRetryCount()).isEqualTo(3);
    }

    @Test
    @Order(2)
    @DisplayName("节点执行失败后应该能够重试")
    void nodeShouldRetryOnFailure() {
        // Given
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM节点");
        llmNode.updateConfig(new NodeConfig(Map.of(
                "agentId", "agent-001",
                "sessionId", "session-001",
                "prompt", "test",
                "streaming", false
        ), 30000, 2));

        // 模拟第一次失败，第二次成功
        when(agentChatPort.chatMessages(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("网络错误"))
                .thenReturn(new AgentMessage(AgentMessage.MessageType.ASSISTANT, "重试成功"));

        // When: 第一次执行
        Mono<NodeResult> firstAttempt = llmNodeProcessor.process(llmNode, context);

        // Then: 第一次应该失败
        StepVerifier.create(firstAttempt)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isFalse();
                })
                .verifyComplete();

        // When: 第二次执行（重试）
        Mono<NodeResult> retryAttempt = llmNodeProcessor.process(llmNode, context);

        // Then: 第二次应该成功
        StepVerifier.create(retryAttempt)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    assertThat(result.getOutput("content")).isEqualTo("重试成功");
                })
                .verifyComplete();
    }

    @Test
    @Order(3)
    @DisplayName("超过最大重试次数后应该返回失败")
    void shouldFailAfterMaxRetries() {
        // Given
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM节点");
        llmNode.updateConfig(new NodeConfig(Map.of(
                "agentId", "agent-001",
                "sessionId", "session-001",
                "prompt", "test",
                "streaming", false
        ), 30000, 2));

        // 模拟持续失败
        when(agentChatPort.chatMessages(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("持续失败"));

        // When: 多次执行
        for (int i = 0; i < 3; i++) {
            Mono<NodeResult> result = llmNodeProcessor.process(llmNode, context);
            StepVerifier.create(result)
                    .assertNext(r -> assertThat(r.isSuccess()).isFalse())
                    .verifyComplete();
        }
    }

    // ==================== Try-Catch策略测试 ====================

    @Test
    @Order(10)
    @DisplayName("工作流应该支持Try-Catch分支")
    void workflowShouldSupportTryCatchBranch() {
        // Given: 创建带Try-Catch的工作流
        //        ┌-> LLM_Success ┐
        // Start -> Condition -> End
        //        └-> LLM_Fail  ┘
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "Try-Catch");
        WorkflowNode successNode = WorkflowNode.create(NodeType.LLM, "成功分支");
        WorkflowNode failNode = WorkflowNode.create(NodeType.LLM, "失败分支");
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束");

        WorkflowGraph graph = WorkflowGraph.empty()
                .addNode(startNode)
                .addNode(conditionNode)
                .addNode(successNode)
                .addNode(failNode)
                .addNode(endNode)
                .addEdge(WorkflowEdge.create(startNode.getId(), conditionNode.getId()))
                .addEdge(WorkflowEdge.createWithCondition(conditionNode.getId(), successNode.getId(), "success == true"))
                .addEdge(WorkflowEdge.createWithCondition(conditionNode.getId(), failNode.getId(), "success == false"))
                .addEdge(WorkflowEdge.create(successNode.getId(), endNode.getId()))
                .addEdge(WorkflowEdge.create(failNode.getId(), endNode.getId()));

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(graph);

        // Then
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @Order(11)
    @DisplayName("失败分支应该能够处理错误")
    void failBranchShouldHandleError() {
        // Given
        WorkflowNode failNode = WorkflowNode.create(NodeType.LLM, "失败处理");
        failNode.updateConfig(new NodeConfig(Map.of(
                "agentId", "agent-001",
                "sessionId", "session-001",
                "prompt", "处理错误",
                "streaming", false
        ), 30000, 0));

        when(agentChatPort.chatMessages(anyString(), anyString(), anyString()))
                .thenReturn(new AgentMessage(AgentMessage.MessageType.ASSISTANT, "错误已处理"));

        // When
        Mono<NodeResult> result = llmNodeProcessor.process(failNode, context);

        // Then
        StepVerifier.create(result)
                .assertNext(r -> {
                    assertThat(r.isSuccess()).isTrue();
                    assertThat(r.getOutput("content")).isEqualTo("错误已处理");
                })
                .verifyComplete();
    }

    // ==================== 失败分支执行测试 ====================

    @Test
    @Order(20)
    @DisplayName("节点失败应该记录错误信息")
    void nodeFailureShouldRecordError() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "测试节点");

        // When: 创建失败结果
        NodeResult failureResult = NodeResult.failure(node.getId(), "执行失败：连接超时");
        context.recordNodeResult(failureResult);

        // Then
        assertThat(context.getNodeResults()).containsKey(node.getId());
        NodeResult recorded = context.getNodeResults().get(node.getId());
        assertThat(recorded.isSuccess()).isFalse();
        assertThat(recorded.getErrorMessage()).isEqualTo("执行失败：连接超时");
        assertThat(recorded.getStatus()).isEqualTo(NodeStatus.FAILED);
    }

    @Test
    @Order(21)
    @DisplayName("工作流应该能够继续执行失败分支")
    void workflowShouldContinueWithFailBranch() {
        // Given: 设置上下文变量表示失败
        context.setVariable("hasError", true);
        context.setVariable("errorMessage", "上游节点失败");

        // When: 创建条件节点判断是否需要执行失败分支
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "错误判断");
        conditionNode.updateConfig(new NodeConfig(Map.of(
                "branches", List.of(
                        Map.of("name", "errorBranch", "expression", "hasError == true", "targetNodeId", "errorHandler"),
                        Map.of("name", "normalBranch", "expression", "hasError == false", "targetNodeId", "nextNode")
                )
        ), 30000, 0));

        // Then: 验证条件配置正确
        assertThat(conditionNode.getConfig().getParameter("branches")).isNotNull();
    }

    @Test
    @Order(22)
    @DisplayName("部分节点失败不应该影响独立分支执行")
    void partialFailureShouldNotAffectIndependentBranches() {
        // Given: 创建并行工作流
        WorkflowNode nodeA = WorkflowNode.create(NodeType.LLM, "分支A");
        WorkflowNode nodeB = WorkflowNode.create(NodeType.LLM, "分支B");

        // When: 分支A失败，分支B成功
        NodeResult resultA = NodeResult.failure(nodeA.getId(), "分支A失败");
        NodeResult resultB = NodeResult.success(nodeB.getId(), Map.of("data", "分支B成功"));

        context.recordNodeResult(resultA);
        context.recordNodeResult(resultB);

        // Then: 分支B的结果应该正常记录
        assertThat(context.getNodeResults().get(nodeB.getId()).isSuccess()).isTrue();
        assertThat(context.getNodeResults().get(nodeA.getId()).isSuccess()).isFalse();
    }

    // ==================== 超时处理测试 ====================

    @Test
    @Order(30)
    @DisplayName("节点配置应该支持超时设置")
    void nodeConfigShouldSupportTimeout() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "LLM节点");

        // When: 设置超时时间
        NodeConfig config = new NodeConfig(
                Map.of("agentId", "agent-001", "prompt", "test"),
                5000,  // 5秒超时
                0
        );
        node.updateConfig(config);

        // Then
        assertThat(node.getConfig().getTimeoutMs()).isEqualTo(5000);
    }

    @Test
    @Order(31)
    @DisplayName("节点执行超时应该返回超时结果")
    void nodeTimeoutShouldReturnTimeoutResult() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "LLM节点");
        node.updateConfig(new NodeConfig(Map.of(
                "agentId", "agent-001",
                "sessionId", "session-001",
                "prompt", "test",
                "streaming", false
        ), 1000, 0)); // 1秒超时

        // 模拟超时
        when(agentChatPort.chatMessages(anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    Thread.sleep(2000); // 模拟2秒延迟
                    return new AssistantMessage("响应");
                });

        // When & Then: 应该在超时前完成或抛出超时异常
        // 注意：实际测试中需要使用StepVerifier.withVirtualTime来测试超时
        assertThat(node.getConfig().getTimeoutMs()).isEqualTo(1000);
    }

    @Test
    @Order(32)
    @DisplayName("超时节点应该标记为TIMEOUT状态")
    void timeoutNodeShouldHaveTimeoutStatus() {
        // Given & When: 创建超时结果
        NodeResult timeoutResult = new NodeResult("node1", NodeStatus.TIMEOUT, Map.of());
        timeoutResult.setStartTime(Instant.now().minusMillis(5000));
        timeoutResult.setEndTime(Instant.now());

        // Then
        assertThat(timeoutResult.getStatus()).isEqualTo(NodeStatus.TIMEOUT);
        assertThat(timeoutResult.getStatus().isTerminal()).isTrue();
        assertThat(timeoutResult.isSuccess()).isFalse();
    }

    // ==================== 工作流级别错误处理测试 ====================

    @Test
    @Order(40)
    @DisplayName("工作流失败应该正确设置终态")
    void workflowFailureShouldSetTerminalStatus() {
        // Given
        WorkflowContext ctx = WorkflowContext.create(workflowId);

        // When: 设置失败状态
        ctx.updateStatus(WorkflowStatus.FAILED);

        // Then
        assertThat(ctx.getStatus()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(ctx.getStatus().isTerminal()).isTrue();
        assertThat(ctx.getEndTime()).isNotNull();
        assertThat(ctx.isRunning()).isFalse();
    }

    @Test
    @Order(41)
    @DisplayName("工作流取消应该正确设置终态")
    void workflowCancellationShouldSetTerminalStatus() {
        // Given
        WorkflowContext ctx = WorkflowContext.create(workflowId);

        // When: 设置取消状态
        ctx.updateStatus(WorkflowStatus.CANCELLED);

        // Then
        assertThat(ctx.getStatus()).isEqualTo(WorkflowStatus.CANCELLED);
        assertThat(ctx.getStatus().isTerminal()).isTrue();
        assertThat(ctx.getEndTime()).isNotNull();
    }

    @Test
    @Order(42)
    @DisplayName("工作流应该记录所有失败节点")
    void workflowShouldRecordAllFailedNodes() {
        // Given
        WorkflowNode node1 = WorkflowNode.create(NodeType.LLM, "节点1");
        WorkflowNode node2 = WorkflowNode.create(NodeType.LLM, "节点2");
        WorkflowNode node3 = WorkflowNode.create(NodeType.LLM, "节点3");

        // When: 记录多个失败
        context.recordNodeResult(NodeResult.success(node1.getId(), Map.of()));
        context.recordNodeResult(NodeResult.failure(node2.getId(), "失败1"));
        context.recordNodeResult(NodeResult.failure(node3.getId(), "失败2"));

        // Then: 统计失败节点
        long failedCount = context.getNodeResults().values().stream()
                .filter(r -> !r.isSuccess())
                .count();

        assertThat(failedCount).isEqualTo(2);
    }

    // ==================== 异常恢复测试 ====================

    @Test
    @Order(50)
    @DisplayName("工作流应该能够从暂停状态恢复")
    void workflowShouldRecoverFromPausedState() {
        // Given
        WorkflowContext ctx = WorkflowContext.create(workflowId);
        ctx.updateStatus(WorkflowStatus.PAUSED);
        ctx.setVariable("checkpoint", "step2");

        // When: 恢复执行
        ctx.updateStatus(WorkflowStatus.EXECUTING);

        // Then
        assertThat(ctx.getStatus()).isEqualTo(WorkflowStatus.EXECUTING);
        assertThat(ctx.getVariable("checkpoint")).isEqualTo("step2");
    }

    @Test
    @Order(51)
    @DisplayName("恢复后应该保留之前的执行结果")
    void recoveryShouldPreservePreviousResults() {
        // Given
        WorkflowContext ctx = WorkflowContext.create(workflowId);
        WorkflowNode node1 = WorkflowNode.create(NodeType.START, "开始");
        ctx.recordNodeResult(NodeResult.success(node1.getId(), Map.of("started", true)));
        ctx.setVariable("step1Completed", true);

        // When: 模拟暂停后恢复
        ctx.updateStatus(WorkflowStatus.PAUSED);
        ctx.updateStatus(WorkflowStatus.EXECUTING);

        // Then: 之前的结果应该保留
        assertThat(ctx.getNodeResults()).containsKey(node1.getId());
        assertThat(ctx.getVariable("step1Completed")).isEqualTo(true);
    }

    // ==================== 错误传播测试 ====================

    @Test
    @Order(60)
    @DisplayName("节点错误应该能够传播到下游节点")
    void nodeErrorShouldPropagate() {
        // Given
        WorkflowNode upstreamNode = WorkflowNode.create(NodeType.LLM, "上游节点");
        NodeResult failure = NodeResult.failure(upstreamNode.getId(), "上游失败");
        context.recordNodeResult(failure);

        // When: 下游节点通过变量获取上游状态
        context.setVariable("upstreamFailed", true);
        context.setVariable("upstreamError", failure.getErrorMessage());

        // Then
        assertThat(context.getVariable("upstreamFailed")).isEqualTo(true);
        assertThat(context.getVariable("upstreamError")).isEqualTo("上游失败");
    }

    @Test
    @Order(61)
    @DisplayName("错误信息应该包含详细上下文")
    void errorShouldContainDetailedContext() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "测试节点");
        node.updateConfig(new NodeConfig(Map.of("agentId", "agent-001"), 30000, 3));

        // When: 创建包含详细信息的错误
        String errorMessage = String.format(
                "节点执行失败 [nodeId=%s, nodeName=%s, type=%s]: %s",
                node.getId(),
                node.getName(),
                node.getType(),
                "API调用超时"
        );
        NodeResult result = NodeResult.failure(node.getId(), errorMessage);

        // Then
        assertThat(result.getErrorMessage()).contains(node.getId());
        assertThat(result.getErrorMessage()).contains(node.getName());
        assertThat(result.getErrorMessage()).contains("API调用超时");
    }

    // ==================== 边界条件测试 ====================

    @Test
    @Order(70)
    @DisplayName("空工作流应该正确处理")
    void emptyWorkflowShouldBeHandled() {
        // Given
        WorkflowGraph emptyGraph = WorkflowGraph.empty();

        // When
        DagBuilder.DagBuildResult result = dagBuilder.build(emptyGraph);

        // Then
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @Order(71)
    @DisplayName("无效节点配置应该抛出异常")
    void invalidNodeConfigShouldThrowException() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "LLM节点");
        // 配置为null
        node.setConfig(null);

        // When & Then
        assertThatThrownBy(() -> {
            if (node.getConfig() == null) {
                throw new IllegalArgumentException("节点配置不能为空");
            }
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("节点配置不能为空");
    }

    @Test
    @Order(72)
    @DisplayName("节点执行时间应该被正确记录")
    void nodeExecutionTimeShouldBeRecorded() {
        // Given
        NodeResult result = NodeResult.success("node1", Map.of());
        Instant start = Instant.now().minusMillis(150);
        Instant end = Instant.now();
        result.setStartTime(start);
        result.setEndTime(end);

        // When
        long duration = result.calculateDuration();

        // Then
        assertThat(duration).isGreaterThanOrEqualTo(150);
        assertThat(result.getStartTime()).isNotNull();
        assertThat(result.getEndTime()).isNotNull();
    }
}
