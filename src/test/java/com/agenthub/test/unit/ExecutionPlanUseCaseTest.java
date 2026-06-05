package com.agenthub.test.unit;

import com.agenthub.application.command.CreatePlanCommand;
import com.agenthub.application.command.PlanStepInput;
import com.agenthub.application.dto.ExecutionPlanOutput;
import com.agenthub.application.dto.PlanStepOutput;
import com.agenthub.application.port.out.repositories.ExecutionPlanRepository;
import com.agenthub.application.usecase.ExecutionPlanUseCase;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.plan.ExecutionPlan;
import com.agenthub.domain.model.plan.PlanStep;
import com.agenthub.domain.model.plan.PlanStatus;
import com.agenthub.domain.model.plan.PlanStepStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ExecutionPlanUseCase 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExecutionPlanUseCaseTest {

    @Mock
    private ExecutionPlanRepository executionPlanRepository;

    private ExecutionPlanUseCase executionPlanUseCase;

    private ExecutionPlan testPlan;
    private PlanStep testStep;

    @BeforeEach
    void setUp() {
        executionPlanUseCase = new ExecutionPlanUseCase(executionPlanRepository);
        testPlan = ExecutionPlan.create("agent-1", "session-1", "测试目标");
        testStep = PlanStep.create(new PlanStep.CreationSpec(
                testPlan.getId(), 1, "步骤1", "webSearch", null));
        testPlan.addStep(testStep);
    }

    @Test
    @Order(1)
    void shouldCreatePlan() {
        CreatePlanCommand command = buildCreateCommand();
        when(executionPlanRepository.save(any())).thenReturn(testPlan);

        ExecutionPlanOutput result = executionPlanUseCase.createPlan(command);

        assertNotNull(result);
        assertEquals("测试目标", result.getGoal());
        assertEquals(PlanStatus.PLANNING.name(), result.getStatus());
        verify(executionPlanRepository).save(any());
    }

    @Test
    @Order(2)
    void shouldGetActivePlan() {
        when(executionPlanRepository.findActiveBySessionId("session-1"))
                .thenReturn(Optional.of(testPlan));

        Optional<ExecutionPlanOutput> result = executionPlanUseCase.getActivePlan("session-1");

        assertTrue(result.isPresent());
        assertEquals(testPlan.getId(), result.get().getId());
    }

    @Test
    @Order(3)
    void shouldReturnEmptyWhenNoActivePlan() {
        when(executionPlanRepository.findActiveBySessionId("session-1"))
                .thenReturn(Optional.empty());

        Optional<ExecutionPlanOutput> result = executionPlanUseCase.getActivePlan("session-1");

        assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    void shouldGetPlanById() {
        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));

        ExecutionPlanOutput result = executionPlanUseCase.getPlan(testPlan.getId());

        assertNotNull(result);
        assertEquals(testPlan.getId(), result.getId());
    }

    @Test
    @Order(5)
    void shouldThrowWhenPlanNotFound() {
        when(executionPlanRepository.findById("nonexistent"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> executionPlanUseCase.getPlan("nonexistent"));
    }

    @Test
    @Order(6)
    void shouldStartExecution() {
        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));
        when(executionPlanRepository.save(any())).thenReturn(testPlan);

        ExecutionPlanOutput result = executionPlanUseCase.startExecution(testPlan.getId());

        assertNotNull(result);
        assertEquals(PlanStatus.EXECUTING.name(), result.getStatus());
    }

    @Test
    @Order(7)
    void shouldUpdateStepStatus() {
        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));
        when(executionPlanRepository.save(any())).thenReturn(testPlan);

        ExecutionPlanOutput result = executionPlanUseCase.updateStep(
                testPlan.getId(), testStep.getId(), "RUNNING", null);

        assertNotNull(result);
        verify(executionPlanRepository).save(any());
    }

    @Test
    @Order(8)
    void shouldCompleteStepWithOutput() {
        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));
        when(executionPlanRepository.save(any())).thenReturn(testPlan);

        ExecutionPlanOutput result = executionPlanUseCase.updateStep(
                testPlan.getId(), testStep.getId(), "COMPLETED", "执行结果");

        assertNotNull(result);
        verify(executionPlanRepository).save(any());
    }

    @Test
    @Order(9)
    void shouldCompletePlan() {
        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));
        when(executionPlanRepository.save(any())).thenReturn(testPlan);

        ExecutionPlanOutput result = executionPlanUseCase.completePlan(
                testPlan.getId(), "任务完成");

        assertNotNull(result);
        assertEquals(PlanStatus.COMPLETED.name(), result.getStatus());
        assertEquals("任务完成", result.getResult());
    }

    @Test
    @Order(10)
    void shouldFailPlan() {
        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));
        when(executionPlanRepository.save(any())).thenReturn(testPlan);

        ExecutionPlanOutput result = executionPlanUseCase.failPlan(
                testPlan.getId(), "执行失败");

        assertNotNull(result);
        assertEquals(PlanStatus.FAILED.name(), result.getStatus());
        assertEquals("执行失败", result.getResult());
    }

    @Test
    @Order(11)
    void shouldCancelPlan() {
        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));
        when(executionPlanRepository.save(any())).thenReturn(testPlan);

        ExecutionPlanOutput result = executionPlanUseCase.cancelPlan(
                testPlan.getId(), "用户取消");

        assertNotNull(result);
        assertEquals(PlanStatus.CANCELLED.name(), result.getStatus());
    }

    @Test
    @Order(12)
    void shouldGetNextSteps() {
        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));

        List<PlanStepOutput> result = executionPlanUseCase.getNextSteps(testPlan.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(PlanStepStatus.PENDING.name(), result.getFirst().getStatus());
    }

    @Test
    @Order(13)
    void shouldGetPlansByAgent() {
        when(executionPlanRepository.findByAgentId("agent-1"))
                .thenReturn(List.of(testPlan));

        List<ExecutionPlanOutput> result = executionPlanUseCase.getPlansByAgent("agent-1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @Order(14)
    void shouldTrackStepDependency() {
        PlanStep step2 = PlanStep.create(new PlanStep.CreationSpec(
                testPlan.getId(), 2, "步骤2", "codeExecution", null));
        step2.setDependencyIds(List.of(testStep.getId()));
        testPlan.addStep(step2);
        testStep.complete("完成");

        when(executionPlanRepository.findById(testPlan.getId()))
                .thenReturn(Optional.of(testPlan));

        List<PlanStepOutput> result = executionPlanUseCase.getNextSteps(testPlan.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("步骤2", result.getFirst().getDescription());
    }

    private CreatePlanCommand buildCreateCommand() {
        CreatePlanCommand command = new CreatePlanCommand();
        command.setAgentId("agent-1");
        command.setSessionId("session-1");
        command.setGoal("测试目标");

        PlanStepInput stepInput = new PlanStepInput();
        stepInput.setDescription("步骤1");
        stepInput.setToolName("webSearch");
        command.setSteps(List.of(stepInput));

        return command;
    }
}
