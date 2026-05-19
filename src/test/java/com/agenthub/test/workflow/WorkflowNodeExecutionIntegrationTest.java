package com.agenthub.test.workflow;

import com.agenthub.application.port.out.agent.AgentChatPort;
import com.agenthub.domain.enums.workflow.NodeStatus;
import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.AgentMessage;
import com.agenthub.domain.model.workflow.*;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import com.agenthub.infrastructure.workflow.processor.impl.*;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 节点执行集成测试。
 * 测试所有节点类型的执行逻辑。
 *
 * @author huangdayu
 */
@SpringBootTest(classes = TestAgentHubApplication.class)
@Import(TestWorkflowConfig.class)

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkflowNodeExecutionIntegrationTest {

    @Autowired
    private StartNodeProcessor startNodeProcessor;

    @Autowired
    private EndNodeProcessor endNodeProcessor;

    @Autowired
    private LlmNodeProcessor llmNodeProcessor;

    @Autowired
    private ConditionNodeProcessor conditionNodeProcessor;

    @Autowired
    private VariableAssignNodeProcessor variableAssignNodeProcessor;

    @Autowired
    private VariableResolver variableResolver;
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

    // ==================== Start节点执行测试 ====================

    @Test
    @Order(1)
    @DisplayName("Start节点应该成功执行并初始化上下文")
    void startNodeShouldExecuteSuccessfully() {
        // Given
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始节点");
        startNode.updateConfig(new NodeConfig(Map.of("initialValue", "test"), 30000, 0));

        // When
        Mono<NodeResult> resultMono = startNodeProcessor.process(startNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getOutput("started")).isEqualTo(true);
                assertThat(result.getOutput("startTime")).isNotNull();
            })
            .verifyComplete();
    }

    @Test
    @Order(2)
    @DisplayName("Start节点应该支持节点类型")
    void startNodeShouldSupportCorrectType() {
        assertThat(startNodeProcessor.getSupportedType()).isEqualTo("START");
        
        WorkflowNode startNode = WorkflowNode.create(NodeType.START, "开始");
        assertThat(startNodeProcessor.supports(startNode)).isTrue();
    }

    // ==================== End节点执行测试 ====================

    @Test
    @Order(10)
    @DisplayName("End节点应该成功执行并汇总结果")
    void endNodeShouldExecuteSuccessfully() {
        // Given
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束节点");
        context.setVariable("finalResult", "处理完成");
        context.recordNodeResult(NodeResult.success("node1", Map.of("data", "test")));

        // When
        Mono<NodeResult> resultMono = endNodeProcessor.process(endNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getOutput("completed")).isEqualTo(true);
                assertThat(result.getOutput("endTime")).isNotNull();
                assertThat(result.getOutput("finalResult")).isEqualTo("处理完成");
            })
            .verifyComplete();
    }

    @Test
    @Order(11)
    @DisplayName("End节点应该正确统计执行结果")
    void endNodeShouldCalculateStatistics() {
        // Given
        WorkflowNode endNode = WorkflowNode.create(NodeType.END, "结束节点");
        context.recordNodeResult(NodeResult.success("node1", Map.of()));
        context.recordNodeResult(NodeResult.success("node2", Map.of()));
        context.recordNodeResult(NodeResult.failure("node3", "失败"));

        // When
        Mono<NodeResult> resultMono = endNodeProcessor.process(endNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> summary = (Map<String, Object>) result.getOutput("summary");
                assertThat(summary).isNotNull();
                assertThat(summary.get("totalNodes")).isEqualTo(3);
                assertThat(summary.get("successNodes")).isEqualTo(2L);
                assertThat(summary.get("failedNodes")).isEqualTo(1L);
            })
            .verifyComplete();
    }

    @Test
    @Order(12)
    @DisplayName("End节点应该支持节点类型")
    void endNodeShouldSupportCorrectType() {
        assertThat(endNodeProcessor.getSupportedType()).isEqualTo("END");
    }

    // ==================== LLM节点执行测试 ====================

    @Test
    @Order(20)
    @DisplayName("LLM节点应该成功执行同步聊天")
    void llmNodeShouldExecuteSyncChat() {
        // Given
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM节点");
        llmNode.updateConfig(new NodeConfig(Map.of(
            "agentId", "agent-001",
            "sessionId", "session-001",
            "prompt", "你好",
            "streaming", false
        ), 30000, 0));

        when(agentChatPort.chatMessages(anyString(), anyString(), anyString()))
                .thenReturn(new AgentMessage(AgentMessage.MessageType.ASSISTANT,"你好，我是AI助手"));

        // When
        Mono<NodeResult> resultMono = llmNodeProcessor.process(llmNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getOutput("content")).isEqualTo("你好，我是AI助手");
            })
            .verifyComplete();
    }

    @Test
    @Order(21)
    @DisplayName("LLM节点应该成功执行流式聊天")
    void llmNodeShouldExecuteStreamingChat() {
        // Given
        WorkflowNode llmNode = WorkflowNode.create(NodeType.LLM, "LLM节点");
        llmNode.updateConfig(new NodeConfig(Map.of(
            "agentId", "agent-001",
            "sessionId", "session-001",
            "prompt", "讲个故事",
            "streaming", true
        ), 30000, 0));

        Flux<AgentMessage> messageFlux = Flux.just(
            new AgentMessage(AgentMessage.MessageType.ASSISTANT, "很"),
            new AgentMessage(AgentMessage.MessageType.ASSISTANT, "久"),
            new AgentMessage(AgentMessage.MessageType.ASSISTANT, "以"),
            new AgentMessage(AgentMessage.MessageType.ASSISTANT, "前")
        );
        when(agentChatPort.streamMessages(anyString(), anyString(), anyString())).thenReturn(messageFlux);

        // When
        Mono<NodeResult> resultMono = llmNodeProcessor.process(llmNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getOutput("count")).isEqualTo(4);
            })
            .verifyComplete();
    }

    @Test
    @Order(22)
    @DisplayName("LLM节点应该支持节点类型")
    void llmNodeShouldSupportCorrectType() {
        assertThat(llmNodeProcessor.getSupportedType()).isEqualTo("LLM");
    }

    // ==================== Condition节点执行测试 ====================

    @Test
    @Order(30)
    @DisplayName("Condition节点应该选择匹配的分支")
    void conditionNodeShouldSelectMatchingBranch() {
        // Given
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "条件判断");
        conditionNode.updateConfig(new NodeConfig(Map.of(
            "branches", List.of(
                Map.of("name", "branchA", "expression", "score > 60", "targetNodeId", "nodeA"),
                Map.of("name", "branchB", "expression", "score <= 60", "targetNodeId", "nodeB")
            )
        ), 30000, 0));

        context.setVariable("score", 85);

        // When
        Mono<NodeResult> resultMono = conditionNodeProcessor.process(conditionNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getOutput("evaluated")).isEqualTo(true);
                assertThat(result.getOutput("branchCount")).isEqualTo(2);
            })
            .verifyComplete();
    }

    @Test
    @Order(31)
    @DisplayName("Condition节点应该选择默认分支当没有匹配时")
    void conditionNodeShouldSelectDefaultBranch() {
        // Given
        WorkflowNode conditionNode = WorkflowNode.create(NodeType.CONDITION, "条件判断");
        conditionNode.updateConfig(new NodeConfig(Map.of(
            "branches", List.of(
                Map.of("name", "branchA", "expression", "value > 100", "targetNodeId", "nodeA")
            )
        ), 30000, 0));

        context.setVariable("value", 50);

        // When
        Mono<NodeResult> resultMono = conditionNodeProcessor.process(conditionNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getOutput("selectedBranch")).isEqualTo("default");
            })
            .verifyComplete();
    }

    @Test
    @Order(32)
    @DisplayName("Condition节点应该支持节点类型")
    void conditionNodeShouldSupportCorrectType() {
        assertThat(conditionNodeProcessor.getSupportedType()).isEqualTo("CONDITION");
    }

    // ==================== VariableAssign节点执行测试 ====================

    @Test
    @Order(40)
    @DisplayName("VariableAssign节点应该成功赋值变量")
    void variableAssignNodeShouldAssignVariables() {
        // Given
        WorkflowNode varNode = WorkflowNode.create(NodeType.VARIABLE, "变量赋值");
        varNode.updateConfig(new NodeConfig(Map.of(
            "assignments", List.of(
                Map.of("name", "result", "value", "processed"),
                Map.of("name", "count", "value", 42)
            )
        ), 30000, 0));

        // When
        Mono<NodeResult> resultMono = variableAssignNodeProcessor.process(varNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                assertThat(result.isSuccess()).isTrue();
                assertThat(context.getVariable("result")).isEqualTo("processed");
                assertThat(context.getVariable("count")).isEqualTo(42);
            })
            .verifyComplete();
    }

    @Test
    @Order(41)
    @DisplayName("VariableAssign节点应该支持表达式赋值")
    void variableAssignNodeShouldSupportExpressionAssignment() {
        // Given
        WorkflowNode varNode = WorkflowNode.create(NodeType.VARIABLE, "变量赋值");
        varNode.updateConfig(new NodeConfig(Map.of(
            "assignments", List.of(
                Map.of("name", "doubled", "expression", "${input * 2}")
            )
        ), 30000, 0));

        context.setVariable("input", 10);

        // When
        Mono<NodeResult> resultMono = variableAssignNodeProcessor.process(varNode, context);

        // Then
        StepVerifier.create(resultMono)
            .assertNext(result -> {
                assertThat(result.isSuccess()).isTrue();
            })
            .verifyComplete();
    }

    @Test
    @Order(42)
    @DisplayName("VariableAssign节点应该支持节点类型")
    void variableAssignNodeShouldSupportCorrectType() {
        assertThat(variableAssignNodeProcessor.getSupportedType()).isEqualTo("VARIABLE");
    }

    // ==================== 节点验证测试 ====================

    @Test
    @Order(50)
    @DisplayName("节点处理器应该验证节点配置")
    void nodeProcessorShouldValidateConfig() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "LLM节点");

        // When & Then
        StepVerifier.create(llmNodeProcessor.validate(node))
            .verifyComplete();
    }

    // ==================== 节点结果测试 ====================

    @Test
    @Order(60)
    @DisplayName("NodeResult应该正确计算执行耗时")
    void nodeResultShouldCalculateDuration() {
        // Given
        NodeResult result = NodeResult.success("node1", Map.of());
        result.setStartTime(java.time.Instant.now().minusMillis(100));
        result.setEndTime(java.time.Instant.now());

        // When
        long duration = result.calculateDuration();

        // Then
        assertThat(duration).isGreaterThanOrEqualTo(100);
    }

    @Test
    @Order(61)
    @DisplayName("NodeResult失败结果应该包含错误信息")
    void nodeResultFailureShouldContainError() {
        // Given & When
        NodeResult result = NodeResult.failure("node1", "执行失败：超时");

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("执行失败：超时");
        assertThat(result.getStatus()).isEqualTo(NodeStatus.FAILED);
    }

    // ==================== 变量解析测试 ====================

    @Test
    @Order(70)
    @DisplayName("VariableResolver应该解析上下文变量")
    void variableResolverShouldResolveContextVariables() {
        // Given
        context.setVariable("name", "张三");
        context.setVariable("age", 25);

        // When
        Object name = variableResolver.resolve("${var.name}", context);
        Object age = variableResolver.resolve("${var.age}", context);

        // Then
        assertThat(name).isEqualTo("张三");
        assertThat(age).isEqualTo(25);
    }

    @Test
    @Order(71)
    @DisplayName("VariableResolver应该解析节点输出变量")
    void variableResolverShouldResolveNodeOutputVariables() {
        // Given
        WorkflowNode node = WorkflowNode.create(NodeType.LLM, "LLM");
        context.recordNodeResult(NodeResult.success(node.getId(), Map.of("response", "AI回复")));

        // When
        Object output = variableResolver.resolve("${node." + node.getId() + ".response}", context);

        // Then
        assertThat(output).isEqualTo("AI回复");
    }

    @Test
    @Order(72)
    @DisplayName("VariableResolver应该解析系统变量")
    void variableResolverShouldResolveSystemVariables() {
        // When
        Object workflowIdValue = variableResolver.resolve("${sys.workflowId}", context);
        Object timestamp = variableResolver.resolve("${sys.timestamp}", context);

        // Then
        assertThat(workflowIdValue).isEqualTo(workflowId);
        assertThat(timestamp).isNotNull();
    }
}
