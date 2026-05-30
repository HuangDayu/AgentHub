package com.agenthub.application.usecase;

import com.agenthub.application.command.CreatePlanCommand;
import com.agenthub.application.command.PlanStepInput;
import com.agenthub.application.dto.ExecutionPlanOutput;
import com.agenthub.application.dto.PlanStepOutput;
import com.agenthub.application.port.out.repositories.ExecutionPlanRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.plan.ExecutionPlan;
import com.agenthub.domain.model.plan.PlanStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 执行计划管理用例，负责计划的创建、查询和状态管理。
 */
@Component
@RequiredArgsConstructor
public class ExecutionPlanUseCase {

    private final ExecutionPlanRepository executionPlanRepository;

    /**
     * 创建执行计划。
     *
     * @param command 创建计划命令
     * @return 计划输出
     */
    public ExecutionPlanOutput createPlan(CreatePlanCommand command) {
        ExecutionPlan plan = ExecutionPlan.create(command.getAgentId(), command.getSessionId(), command.getGoal());
        addStepsFromCommand(plan, command.getSteps());
        ExecutionPlan saved = executionPlanRepository.save(plan);
        return toOutput(saved);
    }

    /**
     * 获取当前活跃计划。
     *
     * @param sessionId 会话ID
     * @return 可选计划输出
     */
    public Optional<ExecutionPlanOutput> getActivePlan(String sessionId) {
        return executionPlanRepository.findActiveBySessionId(sessionId).map(this::toOutput);
    }

    /**
     * 根据ID获取计划。
     *
     * @param planId 计划ID
     * @return 计划输出
     */
    public ExecutionPlanOutput getPlan(String planId) {
        ExecutionPlan plan = findPlan(planId);
        return toOutput(plan);
    }

    /**
     * 获取Agent的所有计划。
     *
     * @param agentId Agent ID
     * @return 计划列表
     */
    public List<ExecutionPlanOutput> getPlansByAgent(String agentId) {
        return executionPlanRepository.findByAgentId(agentId).stream()
                .map(this::toOutput).toList();
    }

    /**
     * 开始执行计划。
     *
     * @param planId 计划ID
     * @return 计划输出
     */
    public ExecutionPlanOutput startExecution(String planId) {
        ExecutionPlan plan = findPlan(planId);
        plan.startExecution();
        return toOutput(executionPlanRepository.save(plan));
    }

    /**
     * 更新步骤状态。
     *
     * @param planId   计划ID
     * @param stepId   步骤ID
     * @param status   新状态
     * @param output   执行结果
     * @return 计划输出
     */
    public ExecutionPlanOutput updateStep(String planId, String stepId, String status, String output) {
        ExecutionPlan plan = findPlan(planId);
        PlanStep step = findStep(plan, stepId);
        applyStepUpdate(step, status, output);
        return toOutput(executionPlanRepository.save(plan));
    }

    /**
     * 标记计划完成。
     *
     * @param planId 计划ID
     * @param result 执行结果
     * @return 计划输出
     */
    public ExecutionPlanOutput completePlan(String planId, String result) {
        ExecutionPlan plan = findPlan(planId);
        plan.complete(result);
        return toOutput(executionPlanRepository.save(plan));
    }

    /**
     * 标记计划失败。
     *
     * @param planId 计划ID
     * @param result 错误信息
     * @return 计划输出
     */
    public ExecutionPlanOutput failPlan(String planId, String result) {
        ExecutionPlan plan = findPlan(planId);
        plan.fail(result);
        return toOutput(executionPlanRepository.save(plan));
    }

    /**
     * 取消计划。
     *
     * @param planId 计划ID
     * @param reason 取消原因
     * @return 计划输出
     */
    public ExecutionPlanOutput cancelPlan(String planId, String reason) {
        ExecutionPlan plan = findPlan(planId);
        plan.cancel(reason);
        return toOutput(executionPlanRepository.save(plan));
    }

    /**
     * 获取下一步可执行步骤。
     *
     * @param planId 计划ID
     * @return 可执行步骤列表
     */
    public List<PlanStepOutput> getNextSteps(String planId) {
        ExecutionPlan plan = findPlan(planId);
        return plan.getExecutableSteps().stream()
                .map(this::toStepOutput).toList();
    }

    private ExecutionPlan findPlan(String planId) {
        return executionPlanRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Execution plan not found: " + planId));
    }

    private PlanStep findStep(ExecutionPlan plan, String stepId) {
        return plan.getSteps().stream()
                .filter(s -> s.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Plan step not found: " + stepId));
    }

    public ExecutionPlanOutput addStepToPlan(String planId, String description,
                                              String toolName, String toolInput) {
        ExecutionPlan plan = findPlan(planId);
        int order = plan.getSteps().size() + 1;
        PlanStep step = PlanStep.create(plan.getId(), order, description, toolName, toolInput);
        plan.addStep(step);
        return toOutput(executionPlanRepository.save(plan));
    }

    private void addStepsFromCommand(ExecutionPlan plan, List<PlanStepInput> stepInputs) {
        if (stepInputs == null) return;
        int index = 0;
        for (PlanStepInput input : stepInputs) {
            PlanStep step = PlanStep.create(plan.getId(), index++, input.getDescription(),
                    input.getToolName(), input.getToolInput());
            step.setDependencyIds(input.getDependsOn());
            plan.addStep(step);
        }
    }

    private void applyStepUpdate(PlanStep step, String status, String output) {
        switch (status) {
            case "RUNNING" -> step.start();
            case "COMPLETED" -> step.complete(output);
            case "FAILED" -> step.fail(output);
            case "SKIPPED" -> step.skip();
            default -> step.setStatus(status);
        }
    }

    private ExecutionPlanOutput toOutput(ExecutionPlan plan) {
        ExecutionPlanOutput output = new ExecutionPlanOutput();
        output.setId(plan.getId());
        output.setAgentId(plan.getAgentId());
        output.setSessionId(plan.getSessionId());
        output.setGoal(plan.getGoal());
        output.setStatus(plan.getStatus());
        output.setCurrentStepIndex(plan.getCurrentStepIndex());
        output.setResult(plan.getResult());
        output.setSteps(plan.getSteps().stream().map(this::toStepOutput).toList());
        output.setStepCount(plan.getStepCount());
        output.setCompletedStepCount(plan.getCompletedStepCount());
        output.setCreatedAt(plan.getCreatedAt());
        output.setUpdatedAt(plan.getUpdatedAt());
        return output;
    }

    private PlanStepOutput toStepOutput(PlanStep step) {
        PlanStepOutput output = new PlanStepOutput();
        output.setId(step.getId());
        output.setPlanId(step.getPlanId());
        output.setOrder(step.getOrder());
        output.setDescription(step.getDescription());
        output.setToolName(step.getToolName());
        output.setToolInput(step.getToolInput());
        output.setStatus(step.getStatus());
        output.setOutput(step.getOutput());
        output.setSubagentId(step.getSubagentId());
        output.setSubsessionId(step.getSubsessionId());
        output.setDependencyIds(step.getDependencyIds());
        output.setCreatedAt(step.getCreatedAt());
        output.setUpdatedAt(step.getUpdatedAt());
        return output;
    }
}
