package com.agenthub.test.unit;

import com.agenthub.application.dto.ExecutionPlanOutput;
import com.agenthub.application.usecase.ExecutionPlanUseCase;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantThreadContext;
import com.agenthub.infrastructure.tools.core_tools.PlanTools;
import com.agenthub.infrastructure.tools.core_tools.dto.PlanStepToolInput;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.agenthub.test.common.TestCommonTools.TENANT_ID;
import static com.agenthub.test.common.TestCommonTools.WORKSPACE_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PlanTools 集成测试，验证执行计划工具的完整功能。
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlanToolsIntegrationTest {

    @Autowired
    private PlanTools planTools;

    @Autowired
    private ExecutionPlanUseCase executionPlanUseCase;

    private static final String agentId = "2054196010662010881";
    private static final String sessionId = "2057834835146399745";
    private String createdPlanId;


    @BeforeAll
    static void init() {
        TenantContextHolder.open(new TenantThreadContext(TENANT_ID, WORKSPACE_ID, agentId, sessionId, "1", "test-user", false));
    }

    @Test
    @Order(1)
    void shouldCreatePlan() {
        PlanStepToolInput step1 = new PlanStepToolInput();
        step1.setDescription("获取数据");
        step1.setToolName("webSearch");

        PlanStepToolInput step2 = new PlanStepToolInput();
        step2.setDescription("分析数据");
        step2.setToolName("codeExecution");
        step2.setDependsOn(List.of("获取数据"));

        // 注意：实际调用需要 ToolContext，这里测试 UseCase 层
        // PlanTools 需要 ToolContext，这里直接测试 ExecutionPlanUseCase
        var command = new com.agenthub.application.command.CreatePlanCommand();
        command.setAgentId("test-agent");
        command.setSessionId("test-session");
        command.setGoal("测试自主规划能力");
        command.setSteps(List.of(
                mapToStepInput(step1),
                mapToStepInput(step2)
        ));

        ExecutionPlanOutput result = executionPlanUseCase.createPlan(command);

        assertNotNull(result);
        assertEquals("测试自主规划能力", result.getGoal());
        assertEquals("PLANNING", result.getStatus());
        assertEquals(2, result.getStepCount());
        createdPlanId = result.getId();
    }

    @Test
    @Order(2)
    void shouldGetCreatedPlan() {
        ExecutionPlanOutput result = executionPlanUseCase.getPlan(createdPlanId);

        assertNotNull(result);
        assertEquals(createdPlanId, result.getId());
        assertEquals("PLANNING", result.getStatus());
    }

    @Test
    @Order(3)
    void shouldStartExecution() {
        ExecutionPlanOutput result = executionPlanUseCase.startExecution(createdPlanId);

        assertNotNull(result);
        assertEquals("EXECUTING", result.getStatus());
    }

    @Test
    @Order(4)
    void shouldGetNextSteps() {
        List<com.agenthub.application.dto.PlanStepOutput> steps =
                executionPlanUseCase.getNextSteps(createdPlanId);

        assertNotNull(steps);
        assertFalse(steps.isEmpty());
        // 第一步没有依赖，应该可执行
        assertEquals("获取数据", steps.getFirst().getDescription());
    }

    @Test
    @Order(5)
    void shouldCompleteFirstStep() {
        com.agenthub.application.dto.PlanStepOutput firstStep =
                executionPlanUseCase.getNextSteps(createdPlanId).getFirst();

        ExecutionPlanOutput result = executionPlanUseCase.updateStep(
                createdPlanId, firstStep.getId(), "COMPLETED", "数据获取完成");

        assertNotNull(result);
        assertEquals(1, result.getCompletedStepCount());
    }

    @Test
    @Order(6)
    void shouldMakeSecondStepExecutable() {
        List<com.agenthub.application.dto.PlanStepOutput> steps =
                executionPlanUseCase.getNextSteps(createdPlanId);

        assertNotNull(steps);
        assertFalse(steps.isEmpty());
        assertEquals("分析数据", steps.getFirst().getDescription());
    }

    @Test
    @Order(7)
    void shouldCompleteSecondStep() {
        com.agenthub.application.dto.PlanStepOutput secondStep =
                executionPlanUseCase.getNextSteps(createdPlanId).getFirst();

        ExecutionPlanOutput result = executionPlanUseCase.updateStep(
                createdPlanId, secondStep.getId(), "COMPLETED", "分析完成");

        assertNotNull(result);
        assertEquals(2, result.getCompletedStepCount());
    }

    @Test
    @Order(8)
    void shouldCompletePlan() {
        ExecutionPlanOutput result = executionPlanUseCase.completePlan(
                createdPlanId, "任务完成，所有步骤执行成功");

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("任务完成，所有步骤执行成功", result.getResult());
        assertEquals(result.getStepCount(), result.getCompletedStepCount());
    }

    @Test
    @Order(9)
    void shouldHandleStepFailure() {
        var command = new com.agenthub.application.command.CreatePlanCommand();
        command.setAgentId("test-agent");
        command.setSessionId("test-session-fail");
        command.setGoal("测试失败场景");
        command.setSteps(List.of(
                createStepInput("会失败的步骤", "unknownTool", null)
        ));

        ExecutionPlanOutput plan = executionPlanUseCase.createPlan(command);
        executionPlanUseCase.startExecution(plan.getId());

        com.agenthub.application.dto.PlanStepOutput step =
                executionPlanUseCase.getNextSteps(plan.getId()).getFirst();

        ExecutionPlanOutput result = executionPlanUseCase.updateStep(
                plan.getId(), step.getId(), "FAILED", "工具不存在");

        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
    }

    @Test
    @Order(10)
    void shouldCancelPlan() {
        var command = new com.agenthub.application.command.CreatePlanCommand();
        command.setAgentId("test-agent");
        command.setSessionId("test-session-cancel");
        command.setGoal("测试取消场景");
        command.setSteps(List.of(
                createStepInput("步骤1", "webSearch", null)
        ));

        ExecutionPlanOutput plan = executionPlanUseCase.createPlan(command);

        ExecutionPlanOutput result = executionPlanUseCase.cancelPlan(
                plan.getId(), "用户主动取消");

        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
    }

    private com.agenthub.application.command.PlanStepInput mapToStepInput(PlanStepToolInput toolInput) {
        com.agenthub.application.command.PlanStepInput input =
                new com.agenthub.application.command.PlanStepInput();
        input.setDescription(toolInput.getDescription());
        input.setToolName(toolInput.getToolName());
        input.setToolInput(toolInput.getToolInput());
        input.setDependsOn(toolInput.getDependsOn());
        return input;
    }

    private com.agenthub.application.command.PlanStepInput createStepInput(
            String description, String toolName, String toolInput) {
        com.agenthub.application.command.PlanStepInput input =
                new com.agenthub.application.command.PlanStepInput();
        input.setDescription(description);
        input.setToolName(toolName);
        input.setToolInput(toolInput);
        return input;
    }
}
