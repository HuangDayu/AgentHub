package com.agenthub.domain.model.plan;

import lombok.Data;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 计划步骤，表示执行计划中的一个原子操作。
 */
@Data
public class PlanStep {
    private String id;
    private String planId;
    private int order;
    private String description;
    private String toolName;
    private String toolInput;
    private String status;
    private String output;
    private String subagentId;
    private String subsessionId;
    private String dependsOn;
    private Instant createdAt;
    private Instant updatedAt;

    public PlanStep() {
        this.id = randomId();
        this.status = PlanStepStatus.PENDING.name();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String planId;
        private final int order;
        private final String description;
        private final String toolName;
        private final String toolInput;

        public CreationSpec(String planId, int order, String description,
                               String toolName, String toolInput) {
            this.planId = planId;
            this.order = order;
            this.description = description;
            this.toolName = toolName;
            this.toolInput = toolInput;
        }
    }

    /**
     * 创建新的计划步骤。
     */
    public static PlanStep create(CreationSpec spec) {
        PlanStep step = new PlanStep();
        step.planId = spec.planId;
        step.order = spec.order;
        step.description = spec.description;
        step.toolName = spec.toolName;
        step.toolInput = spec.toolInput;
        return step;
    }

    /**
     * 标记为执行中。
     *
     * @return 更新后的步骤对象
     */
    public PlanStep start() {
        this.status = PlanStepStatus.RUNNING.name();
        this.updatedAt = Instant.now();
        return this;
    }

    /**
     * 标记为已完成。
     *
     * @param output 执行结果
     * @return 更新后的步骤对象
     */
    public PlanStep complete(String output) {
        this.status = PlanStepStatus.COMPLETED.name();
        this.output = output;
        this.updatedAt = Instant.now();
        return this;
    }

    /**
     * 标记为失败。
     *
     * @param output 错误信息
     * @return 更新后的步骤对象
     */
    public PlanStep fail(String output) {
        this.status = PlanStepStatus.FAILED.name();
        this.output = output;
        this.updatedAt = Instant.now();
        return this;
    }

    /**
     * 标记为已跳过。
     *
     * @return 更新后的步骤对象
     */
    public PlanStep skip() {
        this.status = PlanStepStatus.SKIPPED.name();
        this.updatedAt = Instant.now();
        return this;
    }

    /**
     * 关联子Agent。
     *
     * @param subagentId   子Agent ID
     * @param subsessionId 子会话 ID
     */
    public void assignSubagent(String subagentId, String subsessionId) {
        this.subagentId = subagentId;
        this.subsessionId = subsessionId;
    }

    /**
     * 解析依赖步骤ID列表。
     *
     * @return 依赖的步骤ID列表
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
     * 设置依赖步骤ID列表。
     *
     * @param ids 依赖的步骤ID列表
     */
    public void setDependencyIds(List<String> ids) {
        this.dependsOn = ids != null ? String.join(",", ids) : null;
    }

    /**
     * 判断步骤是否已完成。
     *
     * @return 是否已完成
     */
    public boolean isCompleted() {
        return PlanStepStatus.COMPLETED.name().equals(status);
    }

    /**
     * 判断步骤是否已失败。
     *
     * @return 是否已失败
     */
    public boolean isFailed() {
        return PlanStepStatus.FAILED.name().equals(status);
    }

    /**
     * 判断步骤是否已终结（完成、失败或跳过）。
     *
     * @return 是否已终结
     */
    public boolean isTerminal() {
        return isCompleted() || isFailed() || isSkipped();
    }

    /**
     * 判断步骤是否已跳过。
     *
     * @return 是否已跳过
     */
    public boolean isSkipped() {
        return PlanStepStatus.SKIPPED.name().equals(status);
    }
}
