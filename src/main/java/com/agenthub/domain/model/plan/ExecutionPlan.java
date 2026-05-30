package com.agenthub.domain.model.plan;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 执行计划聚合根，管理Agent自主规划的任务执行流程。
 */
@Data
public class ExecutionPlan {
    private String id;
    private String agentId;
    private String sessionId;
    private String goal;
    private String status;
    private int currentStepIndex;
    private String result;
    private List<PlanStep> steps;
    private Instant createdAt;
    private Instant updatedAt;

    public ExecutionPlan() {
        this.id = randomId();
        this.status = PlanStatus.PLANNING.name();
        this.currentStepIndex = 0;
        this.steps = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 创建新的执行计划。
     *
     * @param agentId   Agent ID
     * @param sessionId 会话 ID
     * @param goal      执行目标
     * @return 新创建的执行计划
     */
    public static ExecutionPlan create(String agentId, String sessionId, String goal) {
        ExecutionPlan plan = new ExecutionPlan();
        plan.agentId = agentId;
        plan.sessionId = sessionId;
        plan.goal = goal;
        return plan;
    }

    /**
     * 添加步骤到计划。
     *
     * @param step 计划步骤
     */
    public void addStep(PlanStep step) {
        step.setOrder(steps.size() + 1);
        step.setPlanId(this.id);
        steps.add(step);
        updatedAt = Instant.now();
    }

    /**
     * 开始执行计划。
     */
    public void startExecution() {
        this.status = PlanStatus.EXECUTING.name();
        this.updatedAt = Instant.now();
    }

    /**
     * 标记计划完成。
     *
     * @param result 执行结果
     */
    public void complete(String result) {
        this.status = PlanStatus.COMPLETED.name();
        this.result = result;
        this.updatedAt = Instant.now();
    }

    /**
     * 标记计划失败。
     *
     * @param result 错误信息
     */
    public void fail(String result) {
        this.status = PlanStatus.FAILED.name();
        this.result = result;
        this.updatedAt = Instant.now();
    }

    /**
     * 取消计划。
     *
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        this.status = PlanStatus.CANCELLED.name();
        this.result = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * 获取下一步可执行的步骤（依赖已满足且状态为PENDING）。
     *
     * @return 可执行步骤
     */
    public Optional<PlanStep> getNextStep() {
        return steps.stream()
                .filter(s -> PlanStepStatus.PENDING.name().equals(s.getStatus()))
                .filter(s -> areDependenciesMet(s))
                .findFirst();
    }

    /**
     * 获取所有可执行步骤（依赖已满足且状态为PENDING）。
     *
     * @return 可执行步骤列表
     */
    public List<PlanStep> getExecutableSteps() {
        return steps.stream()
                .filter(s -> PlanStepStatus.PENDING.name().equals(s.getStatus()))
                .filter(this::areDependenciesMet)
                .toList();
    }

    /**
     * 判断计划是否已完成（所有步骤都已终结）。
     *
     * @return 是否已完成
     */
    public boolean isAllStepsTerminated() {
        return steps.stream().allMatch(PlanStep::isTerminal);
    }

    /**
     * 判断计划是否有失败的步骤。
     *
     * @return 是否有失败步骤
     */
    public boolean hasFailedSteps() {
        return steps.stream().anyMatch(PlanStep::isFailed);
    }

    /**
     * 获取步骤总数。
     *
     * @return 步骤总数
     */
    public int getStepCount() {
        return steps.size();
    }

    /**
     * 获取已完成步骤数。
     *
     * @return 已完成步骤数
     */
    public int getCompletedStepCount() {
        return (int) steps.stream().filter(PlanStep::isCompleted).count();
    }

    private boolean areDependenciesMet(PlanStep step) {
        List<String> depIds = step.getDependencyIds();
        if (depIds.isEmpty()) {
            return true;
        }
        return depIds.stream().allMatch(depId -> steps.stream()
                .filter(s -> s.getId().equals(depId))
                .anyMatch(PlanStep::isCompleted));
    }
}
