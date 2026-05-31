package com.agenthub.domain.model.workflow;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 工作流阶段，表示编排中的一个执行单元。
 */
@Data
@NoArgsConstructor
public class WorkflowStage {
    private String id;
    private String workflowId;
    private int order;
    private String name;
    private String stageType;
    private String systemPrompt;
    private String taskTemplate;
    private List<AgentTask> tasks;
    private String dependsOn;
    private String status;
    private String output;
    private int completedTaskCount;
    private int totalTaskCount;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 创建新的阶段。
     *
     * @param workflowId 所属工作流ID
     * @param order      执行顺序
     * @param name       阶段名称
     * @param stageType  阶段类型
     * @return 新创建的阶段
     */
    public static WorkflowStage create(String workflowId, int order, String name, String stageType) {
        WorkflowStage stage = new WorkflowStage();
        stage.id = randomId();
        stage.workflowId = workflowId;
        stage.order = order;
        stage.name = name;
        stage.stageType = stageType;
        stage.tasks = new ArrayList<>();
        stage.status = WorkflowStageStatus.PENDING.name();
        stage.completedTaskCount = 0;
        stage.totalTaskCount = 0;
        stage.createdAt = Instant.now();
        stage.updatedAt = Instant.now();
        return stage;
    }

    /**
     * 添加任务到阶段。
     *
     * @param task 任务
     */
    public void addTask(AgentTask task) {
        tasks.add(task);
        totalTaskCount = tasks.size();
        updatedAt = Instant.now();
    }

    /**
     * 标记为执行中。
     */
    public void start() {
        this.status = WorkflowStageStatus.RUNNING.name();
        this.updatedAt = Instant.now();
    }

    /**
     * 标记为已完成。
     *
     * @param output 阶段输出
     */
    public void complete(String output) {
        this.status = WorkflowStageStatus.COMPLETED.name();
        this.output = output;
        this.completedTaskCount = totalTaskCount;
        this.updatedAt = Instant.now();
    }

    /**
     * 标记为失败。
     *
     * @param output 错误信息
     */
    public void fail(String output) {
        this.status = WorkflowStageStatus.FAILED.name();
        this.output = output;
        this.updatedAt = Instant.now();
    }

    /**
     * 获取依赖阶段ID列表。
     *
     * @return 依赖的阶段ID列表
     */
    public List<String> getDependencyIds() {
        if (dependsOn == null || dependsOn.isBlank()) {
            return List.of();
        }
        return Arrays.stream(dependsOn.split(","))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 设置依赖阶段ID列表。
     *
     * @param ids 依赖的阶段ID列表
     */
    public void setDependencyIds(List<String> ids) {
        this.dependsOn = ids != null ? String.join(",", ids) : null;
    }

    /**
     * 更新已完成任务计数。
     */
    public void refreshCompletedCount() {
        this.completedTaskCount = (int) tasks.stream()
                .filter(AgentTask::isCompleted).count();
        this.updatedAt = Instant.now();
    }

    /**
     * 判断阶段是否已终结。
     *
     * @return 是否已终结
     */
    public boolean isTerminal() {
        return isCompleted() || isFailed() || isSkipped();
    }

    /**
     * 判断阶段是否已完成。
     *
     * @return 是否已完成
     */
    public boolean isCompleted() {
        return WorkflowStageStatus.COMPLETED.name().equals(status);
    }

    /**
     * 判断阶段是否已失败。
     *
     * @return 是否已失败
     */
    public boolean isFailed() {
        return WorkflowStageStatus.FAILED.name().equals(status);
    }

    /**
     * 判断阶段是否已跳过。
     *
     * @return 是否已跳过
     */
    public boolean isSkipped() {
        return WorkflowStageStatus.SKIPPED.name().equals(status);
    }
}
