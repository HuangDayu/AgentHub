package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.agenthub.application.command.AddStepCommand;
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
        if (step.isFailed()) {
            plan.fail(output);
        }
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

    public ExecutionPlanOutput addStepToPlan(AddStepCommand command) {
        ExecutionPlan plan = findPlan(command.getPlanId());
        int order = plan.getSteps().size() + 1;
        PlanStep.CreationSpec request = new PlanStep.CreationSpec(
                plan.getId(), order, command.getDescription(), command.getToolName(), command.getToolInput());
        PlanStep step = PlanStep.create(request);
        plan.addStep(step);
        return toOutput(executionPlanRepository.save(plan));
    }

    private void addStepsFromCommand(ExecutionPlan plan, List<PlanStepInput> stepInputs) {
        if (stepInputs == null) return;
        appendAllSteps(plan, stepInputs);
        resolveStepDependencies(plan, stepInputs);
    }

    private void appendAllSteps(ExecutionPlan plan, List<PlanStepInput> stepInputs) {
        int index = 0;
        for (PlanStepInput input : stepInputs) {
            PlanStep.CreationSpec spec = new PlanStep.CreationSpec(plan.getId(), index++,
                    input.getDescription(), input.getToolName(), input.getToolInput());
            plan.addStep(PlanStep.create(spec));
        }
    }

    private void resolveStepDependencies(ExecutionPlan plan, List<PlanStepInput> stepInputs) {
        for (int i = 0; i < stepInputs.size(); i++) {
            applyStepDependency(plan, stepInputs.get(i), plan.getSteps().get(i));
        }
    }

    private void applyStepDependency(ExecutionPlan plan, PlanStepInput input, PlanStep step) {
        if (input.getDependsOn() == null || input.getDependsOn().isEmpty()) return;
        List<String> resolved = input.getDependsOn().stream()
                .map(ref -> resolveDependencyRef(plan, ref)).toList();
        step.setDependencyIds(resolved);
    }

    private String resolveDependencyRef(ExecutionPlan plan, String ref) {
        return plan.getSteps().stream()
                .filter(s -> ref.equals(s.getId()) || ref.equals(s.getDescription()))
                .map(PlanStep::getId)
                .findFirst()
                .orElse(ref);
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
        BeanUtil.copyProperties(plan, output, CopyOptions.create().setIgnoreProperties("steps"));
        output.setStepCount(plan.getStepCount());
        output.setCompletedStepCount(plan.getCompletedStepCount());
        output.setSteps(plan.getSteps().stream().map(this::toStepOutput).toList());
        return output;
    }

    private PlanStepOutput toStepOutput(PlanStep step) {
        PlanStepOutput output = new PlanStepOutput();
        BeanUtil.copyProperties(step, output);
        return output;
    }
}
