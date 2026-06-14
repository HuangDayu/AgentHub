package com.agenthub.test.unit;

import com.agenthub.application.command.CreatePlanCommand;
import com.agenthub.application.command.PlanStepInput;
import com.agenthub.application.dto.ExecutionPlanOutput;
import com.agenthub.application.dto.PlanStepOutput;
import com.agenthub.application.usecase.ExecutionPlanUseCase;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantThreadContext;
import com.agenthub.infrastructure.tools.base_tools.KnowledgeTools;
import com.agenthub.infrastructure.tools.base_tools.ModelTools;
import com.agenthub.infrastructure.tools.core_tools.dto.KnowledgeBaseSummary;
import com.agenthub.infrastructure.tools.core_tools.dto.ModelCapabilitySummary;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static com.agenthub.test.common.TestCommonTools.TENANT_ID;
import static com.agenthub.test.common.TestCommonTools.WORKSPACE_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 自主 Agent 完整流程测试，模拟 Agent 自主规划和执行任务的完整流程。
 */
@SpringBootTest(classes = TestAgentHubApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutonomousAgentWorkflowTest {

    @Autowired
    private ExecutionPlanUseCase executionPlanUseCase;

    @Autowired
    private ModelTools modelTools;

    @Autowired
    private KnowledgeTools knowledgeTools;
    private static final String agentId = "2054196010662010881";
    private static final String sessionId = "2057834835146399745";
    private ExecutionPlanOutput currentPlan;


    @BeforeAll
    void init() {
        TenantContextHolder.open(new TenantThreadContext(TENANT_ID, WORKSPACE_ID, agentId, sessionId, "1", "test-user", false));
    }

    @Test
    @Order(1)
    void step1_discoverAvailableModels() {
        List<ModelCapabilitySummary> capabilities = modelTools.getModelCapabilities();
        assertNotNull(capabilities);
        System.out.println("=== 可用模型 ===");
        for (ModelCapabilitySummary cap : capabilities) {
            System.out.printf("  - %s (%s): %s, 成本=%s, 速度=%s%n",
                    cap.getModelName(), cap.getSupplier(),
                    cap.getCapabilityDomain(), cap.getCostLevel(), cap.getSpeedLevel());
        }
    }

    @Test
    @Order(2)
    void step2_discoverAvailableKnowledgeBases() {
        List<KnowledgeBaseSummary> summaries = knowledgeTools.getKnowledgeBaseSummaries(null);
        assertNotNull(summaries);
        System.out.println("=== 可用知识库 ===");
        for (KnowledgeBaseSummary kb : summaries) {
            System.out.printf("  - %s: %s (文档数=%d)%n",
                    kb.getName(), kb.getDescription(), kb.getDocumentCount());
        }
    }

    @Test
    @Order(3)
    void step3_createExecutionPlan() {
        currentPlan = executionPlanUseCase.createPlan(buildResearchPlanCommand());
        assertNotNull(currentPlan);
        assertEquals("PLANNING", currentPlan.getStatus());
        assertEquals(4, currentPlan.getStepCount());
        System.out.println("=== 创建执行计划 ===");
        System.out.printf("  计划ID: %s%n", currentPlan.getId());
        System.out.printf("  目标: %s%n", currentPlan.getGoal());
        System.out.printf("  步骤数: %d%n", currentPlan.getStepCount());
    }

    @Test
    @Order(4)
    void step4_startExecution() {
        ExecutionPlanOutput plan = executionPlanUseCase.startExecution(currentPlan.getId());
        assertEquals("EXECUTING", plan.getStatus());
        System.out.println("=== 开始执行 ===");
    }

    @Test
    @Order(5)
    void step5_executeStep1_search() {

        List<PlanStepOutput> nextSteps = executionPlanUseCase.getNextSteps(currentPlan.getId());
        assertFalse(nextSteps.isEmpty());
        PlanStepOutput step1 = nextSteps.getFirst();
        assertEquals("搜索相关信息", step1.getDescription());
        executionPlanUseCase.updateStep(currentPlan.getId(), step1.getId(), "RUNNING", null);
        executionPlanUseCase.updateStep(currentPlan.getId(), step1.getId(), "COMPLETED",
                "搜索完成，找到10条相关结果");
        System.out.println("=== 步骤1完成: 搜索信息 ===");
    }

    @Test
    @Order(6)
    void step6_executeStep2_analyze() {

        List<PlanStepOutput> nextSteps = executionPlanUseCase.getNextSteps(currentPlan.getId());
        assertFalse(nextSteps.isEmpty());
        PlanStepOutput step2 = nextSteps.getFirst();
        assertEquals("分析数据", step2.getDescription());
        executionPlanUseCase.updateStep(currentPlan.getId(), step2.getId(), "RUNNING", null);
        executionPlanUseCase.updateStep(currentPlan.getId(), step2.getId(), "COMPLETED",
                "分析完成，发现3个关键洞察");
        System.out.println("=== 步骤2完成: 分析数据 ===");

    }

    @Test
    @Order(7)
    void step7_executeStep3_generate() {

        List<PlanStepOutput> nextSteps = executionPlanUseCase.getNextSteps(currentPlan.getId());
        assertFalse(nextSteps.isEmpty());
        PlanStepOutput step3 = nextSteps.getFirst();
        assertEquals("生成报告", step3.getDescription());
        executionPlanUseCase.updateStep(currentPlan.getId(), step3.getId(), "RUNNING", null);
        executionPlanUseCase.updateStep(currentPlan.getId(), step3.getId(), "COMPLETED",
                "报告已生成: research-report.md");
        System.out.println("=== 步骤3完成: 生成报告 ===");

    }

    @Test
    @Order(8)
    void step8_executeStep4_review() {

        List<PlanStepOutput> nextSteps = executionPlanUseCase.getNextSteps(currentPlan.getId());
        assertFalse(nextSteps.isEmpty());
        PlanStepOutput step4 = nextSteps.getFirst();
        assertEquals("审核报告", step4.getDescription());
        executionPlanUseCase.updateStep(currentPlan.getId(), step4.getId(), "RUNNING", null);
        executionPlanUseCase.updateStep(currentPlan.getId(), step4.getId(), "COMPLETED",
                "审核通过，报告质量良好");
        System.out.println("=== 步骤4完成: 审核报告 ===");

    }

    @Test
    @Order(9)
    void step9_completePlan() {
        ExecutionPlanOutput plan = executionPlanUseCase.completePlan(
                currentPlan.getId(), "研究报告已完成，包含3个关键洞察，已通过审核");
        assertEquals("COMPLETED", plan.getStatus());
        assertEquals(4, plan.getCompletedStepCount());
        assertEquals(plan.getStepCount(), plan.getCompletedStepCount());
        System.out.println("=== 计划完成 ===");
        System.out.printf("  完成步骤数: %d/%d%n", plan.getCompletedStepCount(), plan.getStepCount());
        System.out.printf("  最终结果: %s%n", plan.getResult());
    }

    @Test
    @Order(10)
    void step10_verifyPlanHistory() {
        List<ExecutionPlanOutput> plans = executionPlanUseCase.getPlansByAgent(agentId);
        assertNotNull(plans);
        assertFalse(plans.isEmpty());
        ExecutionPlanOutput completedPlan = plans.stream()
                .filter(p -> p.getId().equals(currentPlan.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(completedPlan);
        assertEquals("COMPLETED", completedPlan.getStatus());
        assertEquals(4, completedPlan.getStepCount());
        assertEquals(4, completedPlan.getCompletedStepCount());
        System.out.println("=== 验证计划历史 ===");
        System.out.printf("  Agent 历史计划数: %d%n", plans.size());
    }

    private CreatePlanCommand buildResearchPlanCommand() {
        CreatePlanCommand command = new CreatePlanCommand();
        command.setAgentId(agentId);
        command.setSessionId(sessionId);
        command.setGoal("完成一份关于AI Agent自主规划能力的研究报告");
        command.setSteps(buildPlanSteps());
        return command;
    }

    private List<PlanStepInput> buildPlanSteps() {
        List<PlanStepInput> steps = new ArrayList<>();
        steps.add(createStep("搜索相关信息", "webSearch", null));
        steps.add(createStep("分析数据", "codeExecution", List.of("搜索相关信息")));
        steps.add(createStep("生成报告", "write", List.of("分析数据")));
        steps.add(createStep("审核报告", null, List.of("生成报告")));
        return steps;
    }

    private PlanStepInput createStep(String description, String toolName, List<String> dependsOn) {
        PlanStepInput step = new PlanStepInput();
        step.setDescription(description);
        step.setToolName(toolName);
        step.setDependsOn(dependsOn);
        return step;
    }
}
