package com.agenthub.domain.model.workflow;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 动态工作流聚合根，管理Agent自主编排的并行子Agent执行流程。
 */
@Data
@NoArgsConstructor
public class DynamicWorkflow {
    private String id;
    private String agentId;
    private String sessionId;
    private String task;
    private String pattern;
    private String status;
    private List<WorkflowStage> stages;
    private String result;
    private int maxConcurrentAgents;
    private int totalTokensUsed;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 创建新的动态工作流。
     *
     * @param agentId   所属Agent ID
     * @param sessionId 会话ID
     * @param task      任务描述
     * @return 新创建的工作流
     */
    public static DynamicWorkflow create(String agentId, String sessionId, String task) {
        DynamicWorkflow workflow = new DynamicWorkflow();
        Instant now = Instant.now();
        applyIdentity(workflow, new WorkflowIdentity(agentId, sessionId, task));
        applyDefaults(workflow, now);
        return workflow;
    }

    private static void applyIdentity(DynamicWorkflow workflow, WorkflowIdentity identity) {
        workflow.id = randomId();
        workflow.agentId = identity.agentId();
        workflow.sessionId = identity.sessionId();
        workflow.task = identity.task();
    }

    private static void applyDefaults(DynamicWorkflow workflow, Instant now) {
        workflow.pattern = WorkflowPattern.FAN_OUT.name();
        workflow.status = DynamicWorkflowStatus.PLANNING.name();
        workflow.stages = new ArrayList<>();
        workflow.maxConcurrentAgents = 4;
        workflow.totalTokensUsed = 0;
        workflow.createdAt = now;
        workflow.updatedAt = now;
    }

    /**
     * 动态工作流身份信息（private static final 不可变载体）。
     */
    private static final class WorkflowIdentity {
        private final String agentId;
        private final String sessionId;
        private final String task;

        private WorkflowIdentity(String agentId, String sessionId, String task) {
            this.agentId = agentId;
            this.sessionId = sessionId;
            this.task = task;
        }

        public String agentId() { return agentId; }
        public String sessionId() { return sessionId; }
        public String task() { return task; }
    }

    /**
     * 添加阶段到工作流。
     *
     * @param stage 工作流阶段
     */
    public void addStage(WorkflowStage stage) {
        stage.setOrder(stages.size() + 1);
        stage.setWorkflowId(this.id);
        stages.add(stage);
        updatedAt = Instant.now();
    }

    /**
     * 开始执行工作流。
     */
    public void startExecution() {
        this.status = DynamicWorkflowStatus.EXECUTING.name();
        this.updatedAt = Instant.now();
    }

    /**
     * 开始验证阶段。
     */
    public void startVerification() {
        this.status = DynamicWorkflowStatus.VERIFYING.name();
        this.updatedAt = Instant.now();
    }

    /**
     * 标记工作流完成。
     *
     * @param result 最终结果
     */
    public void complete(String result) {
        this.status = DynamicWorkflowStatus.COMPLETED.name();
        this.result = result;
        this.updatedAt = Instant.now();
    }

    /**
     * 标记工作流失败。
     *
     * @param result 错误信息
     */
    public void fail(String result) {
        this.status = DynamicWorkflowStatus.FAILED.name();
        this.result = result;
        this.updatedAt = Instant.now();
    }

    /**
     * 取消工作流。
     *
     * @param reason 取消原因
     */
    public void cancel(String reason) {
        this.status = DynamicWorkflowStatus.CANCELLED.name();
        this.result = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * 获取所有可执行阶段（依赖已满足且状态为PENDING）。
     *
     * @return 可执行阶段列表
     */
    public List<WorkflowStage> getExecutableStages() {
        return stages.stream()
                .filter(s -> WorkflowStageStatus.PENDING.name().equals(s.getStatus()))
                .filter(this::areDependenciesMet)
                .toList();
    }

    /**
     * 判断是否所有阶段都已终结。
     *
     * @return 是否所有阶段已终结
     */
    public boolean isAllStagesTerminated() {
        return stages.stream().allMatch(WorkflowStage::isTerminal);
    }

    /**
     * 判断是否有失败的阶段。
     *
     * @return 是否有失败阶段
     */
    public boolean hasFailedStages() {
        return stages.stream().anyMatch(WorkflowStage::isFailed);
    }

    /**
     * 获取阶段总数。
     *
     * @return 阶段总数
     */
    public int getStageCount() {
        return stages.size();
    }

    /**
     * 获取已完成阶段数。
     *
     * @return 已完成阶段数
     */
    public int getCompletedStageCount() {
        return (int) stages.stream().filter(WorkflowStage::isCompleted).count();
    }

    /**
     * 获取进度百分比。
     *
     * @return 进度百分比（0-100）
     */
    public int getProgressPercent() {
        if (stages.isEmpty()) return 0;
        return (int) ((getCompletedStageCount() * 100L) / stages.size());
    }

    /**
     * 获取阶段摘要。
     *
     * @return 阶段摘要列表
     */
    public List<String> getStageSummaries() {
        return stages.stream()
                .map(s -> s.getName() + " [" + s.getStatus() + "]")
                .collect(Collectors.toList());
    }

    private boolean areDependenciesMet(WorkflowStage stage) {
        List<String> depIds = stage.getDependencyIds();
        if (depIds.isEmpty()) return true;
        return depIds.stream().allMatch(depId -> stages.stream()
                .filter(s -> s.getId().equals(depId))
                .anyMatch(WorkflowStage::isCompleted));
    }
}
