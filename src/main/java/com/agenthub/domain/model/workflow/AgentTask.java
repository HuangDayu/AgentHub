package com.agenthub.domain.model.workflow;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 工作流任务，表示分配给子Agent的原子执行单元。
 */
@Data
@NoArgsConstructor
public class AgentTask {
    private String id;
    private String stageId;
    private String workflowId;
    private String taskDescription;
    private String subagentId;
    private String subsessionId;
    private String status;
    private String result;
    private String modelConfigId;
    private List<String> toolNames;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 创建新的任务。
     *
     * @param stageId         所属阶段ID
     * @param workflowId      所属工作流ID
     * @param taskDescription 任务描述
     * @return 新创建的任务
     */
    public static AgentTask create(String stageId, String workflowId, String taskDescription) {
        AgentTask task = new AgentTask();
        task.initTask(stageId, workflowId, taskDescription);
        return task;
    }

    private void initTask(String stageId, String workflowId, String taskDescription) {
        Instant now = Instant.now();
        this.id = randomId();
        this.stageId = stageId;
        this.workflowId = workflowId;
        this.taskDescription = taskDescription;
        this.status = AgentTaskStatus.PENDING.name();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 标记为执行中。
     */
    public void start() {
        this.status = AgentTaskStatus.RUNNING.name();
        this.updatedAt = Instant.now();
    }

    /**
     * 标记为已完成。
     *
     * @param result 执行结果
     */
    public void complete(String result) {
        this.status = AgentTaskStatus.COMPLETED.name();
        this.result = result;
        this.updatedAt = Instant.now();
    }

    /**
     * 标记为失败。
     *
     * @param result 错误信息
     */
    public void fail(String result) {
        this.status = AgentTaskStatus.FAILED.name();
        this.result = result;
        this.updatedAt = Instant.now();
    }

    /**
     * 关联子Agent运行时信息。
     *
     * @param subagentId   子Agent ID
     * @param subsessionId 子会话ID
     */
    public void assignSubagent(String subagentId, String subsessionId) {
        this.subagentId = subagentId;
        this.subsessionId = subsessionId;
        this.updatedAt = Instant.now();
    }

    /**
     * 判断任务是否已终结。
     *
     * @return 是否已终结
     */
    public boolean isTerminal() {
        return isCompleted() || isFailed();
    }

    /**
     * 判断任务是否已完成。
     *
     * @return 是否已完成
     */
    public boolean isCompleted() {
        return AgentTaskStatus.COMPLETED.name().equals(status);
    }

    /**
     * 判断任务是否已失败。
     *
     * @return 是否已失败
     */
    public boolean isFailed() {
        return AgentTaskStatus.FAILED.name().equals(status);
    }
}
