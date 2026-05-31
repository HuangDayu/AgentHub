package com.agenthub.application.usecase;

import com.agenthub.application.command.CreateWorkflowCommand;
import com.agenthub.application.dto.DynamicWorkflowOutput;
import com.agenthub.application.dto.DynamicWorkflowSummary;
import com.agenthub.application.dto.WorkflowStageOutput;
import com.agenthub.application.port.out.DynamicWorkflowPort;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.workflow.DynamicWorkflow;
import com.agenthub.domain.model.workflow.WorkflowStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 动态工作流用例，管理工作流的创建、查询和状态更新。
 */
@Component
@RequiredArgsConstructor
public class DynamicWorkflowUseCase {

    private final DynamicWorkflowPort workflowPort;

    /**
     * 创建动态工作流。
     *
     * @param command 创建命令
     * @return 工作流输出
     */
    public DynamicWorkflowOutput createWorkflow(CreateWorkflowCommand command) {
        DynamicWorkflow workflow = buildWorkflow(command);
        addStages(workflow, command.getStages());
        workflowPort.save(workflow);
        return toOutput(workflow);
    }

    /**
     * 获取工作流详情。
     *
     * @param workflowId 工作流ID
     * @return 工作流输出
     */
    public DynamicWorkflowOutput getWorkflow(String workflowId) {
        DynamicWorkflow workflow = findWorkflow(workflowId);
        return toOutput(workflow);
    }

    /**
     * 获取工作流领域模型（供引擎内部使用）。
     *
     * @param workflowId 工作流ID
     * @return 工作流领域模型
     */
    public DynamicWorkflow getWorkflowDomain(String workflowId) {
        return findWorkflow(workflowId);
    }

    /**
     * 获取工作流摘要列表。
     *
     * @param sessionId 会话ID
     * @return 摘要列表
     */
    public List<DynamicWorkflowSummary> listBySession(String sessionId) {
        return workflowPort.findBySessionId(sessionId).stream()
                .map(this::toSummary).collect(Collectors.toList());
    }

    /**
     * 更新工作流状态。
     *
     * @param workflowId 工作流ID
     * @param status     状态
     */
    public void updateStatus(String workflowId, String status) {
        DynamicWorkflow workflow = findWorkflow(workflowId);
        applyStatus(workflow, status);
        workflowPort.save(workflow);
    }

    /**
     * 更新阶段状态。
     *
     * @param stageId 阶段ID
     * @param status  状态
     */
    public void updateStageStatus(String stageId, String status) {
        WorkflowStage stage = findStage(stageId);
        applyStageStatus(stage, status);
        workflowPort.saveStage(stage);
    }

    /**
     * 更新阶段输出。
     *
     * @param stageId 阶段ID
     * @param output  输出内容
     */
    public void updateStageOutput(String stageId, String output) {
        WorkflowStage stage = findStage(stageId);
        stage.complete(output);
        workflowPort.saveStage(stage);
    }

    /**
     * 更新任务结果。
     *
     * @param taskId 任务ID
     * @param result 执行结果
     * @param status 任务状态
     */
    public void updateTaskResult(String taskId, String result, String status) {
        com.agenthub.domain.model.workflow.AgentTask task = findTask(taskId);
        applyTaskResult(task, result, status);
        workflowPort.saveTask(task);
    }

    /**
     * 标记工作流完成。
     *
     * @param workflowId 工作流ID
     * @param result     最终结果
     */
    public void completeWorkflow(String workflowId, String result) {
        DynamicWorkflow workflow = findWorkflow(workflowId);
        workflow.complete(result);
        workflowPort.save(workflow);
    }

    /**
     * 标记工作流失败。
     *
     * @param workflowId 工作流ID
     * @param reason     失败原因
     */
    public void failWorkflow(String workflowId, String reason) {
        DynamicWorkflow workflow = findWorkflow(workflowId);
        workflow.fail(reason);
        workflowPort.save(workflow);
    }

    private DynamicWorkflow findWorkflow(String workflowId) {
        return workflowPort.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("工作流不存在: " + workflowId));
    }

    private WorkflowStage findStage(String stageId) {
        return workflowPort.findStagesByWorkflowId(null).stream()
                .filter(s -> s.getId().equals(stageId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("阶段不存在: " + stageId));
    }

    private com.agenthub.domain.model.workflow.AgentTask findTask(String taskId) {
        return workflowPort.findTasksByWorkflowId(null).stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("任务不存在: " + taskId));
    }

    private DynamicWorkflow buildWorkflow(CreateWorkflowCommand command) {
        DynamicWorkflow workflow = DynamicWorkflow.create(
                command.getAgentId(),
                command.getSessionId(),
                command.getTask());
        workflow.setPattern(command.getPattern());
        return workflow;
    }

    private void addStages(DynamicWorkflow workflow, List<CreateWorkflowCommand.StageInput> stageInputs) {
        if (stageInputs == null) return;
        for (CreateWorkflowCommand.StageInput input : stageInputs) {
            addStage(workflow, input);
        }
    }

    private void addStage(DynamicWorkflow workflow, CreateWorkflowCommand.StageInput input) {
        WorkflowStage stage = WorkflowStage.create(
                workflow.getId(), workflow.getStageCount() + 1,
                input.getName(), input.getStageType());
        stage.setSystemPrompt(input.getSystemPrompt());
        stage.setTaskTemplate(input.getTaskTemplate());
        stage.setDependencyIds(input.getDependsOn());
        addTasksToStage(stage, input.getTasks());
        workflow.addStage(stage);
    }

    private void addTasksToStage(WorkflowStage stage, List<CreateWorkflowCommand.TaskInput> taskInputs) {
        if (taskInputs == null) return;
        for (CreateWorkflowCommand.TaskInput input : taskInputs) {
            addTaskToStage(stage, input);
        }
    }

    private void addTaskToStage(WorkflowStage stage, CreateWorkflowCommand.TaskInput input) {
        com.agenthub.domain.model.workflow.AgentTask task = com.agenthub.domain.model.workflow.AgentTask.create(
                stage.getId(), stage.getWorkflowId(), input.getTaskDescription());
        task.setModelConfigId(input.getModelConfigId());
        task.setToolNames(input.getToolNames());
        stage.addTask(task);
    }

    private void applyStatus(DynamicWorkflow workflow, String status) {
        switch (status) {
            case "EXECUTING" -> workflow.startExecution();
            case "VERIFYING" -> workflow.startVerification();
            case "COMPLETED" -> workflow.complete(workflow.getResult());
            case "FAILED" -> workflow.fail(workflow.getResult());
            case "CANCELLED" -> workflow.cancel("手动取消");
        }
    }

    private void applyStageStatus(WorkflowStage stage, String status) {
        switch (status) {
            case "RUNNING" -> stage.start();
            case "COMPLETED" -> stage.complete(stage.getOutput());
            case "FAILED" -> stage.fail(stage.getOutput());
        }
    }

    private void applyTaskResult(com.agenthub.domain.model.workflow.AgentTask task, String result, String status) {
        if ("COMPLETED".equals(status)) {
            task.complete(result);
        } else {
            task.fail(result);
        }
    }

    private DynamicWorkflowOutput toOutput(DynamicWorkflow workflow) {
        DynamicWorkflowOutput output = new DynamicWorkflowOutput();
        output.setId(workflow.getId());
        output.setAgentId(workflow.getAgentId());
        output.setSessionId(workflow.getSessionId());
        output.setTask(workflow.getTask());
        output.setPattern(workflow.getPattern());
        output.setStatus(workflow.getStatus());
        output.setResult(workflow.getResult());
        output.setMaxConcurrentAgents(workflow.getMaxConcurrentAgents());
        output.setTotalTokensUsed(workflow.getTotalTokensUsed());
        output.setProgressPercent(workflow.getProgressPercent());
        output.setStageCount(workflow.getStageCount());
        output.setCompletedStageCount(workflow.getCompletedStageCount());
        output.setCreatedAt(workflow.getCreatedAt());
        output.setUpdatedAt(workflow.getUpdatedAt());
        output.setStages(toStageOutputs(workflow));
        return output;
    }

    private List<WorkflowStageOutput> toStageOutputs(DynamicWorkflow workflow) {
        return workflow.getStages().stream().map(this::toStageOutput).collect(Collectors.toList());
    }

    private WorkflowStageOutput toStageOutput(WorkflowStage stage) {
        WorkflowStageOutput output = new WorkflowStageOutput();
        output.setId(stage.getId());
        output.setWorkflowId(stage.getWorkflowId());
        output.setOrder(stage.getOrder());
        output.setName(stage.getName());
        output.setStageType(stage.getStageType());
        output.setSystemPrompt(stage.getSystemPrompt());
        output.setTaskTemplate(stage.getTaskTemplate());
        output.setDependencyIds(stage.getDependencyIds());
        output.setStatus(stage.getStatus());
        output.setOutput(stage.getOutput());
        output.setCompletedTaskCount(stage.getCompletedTaskCount());
        output.setTotalTaskCount(stage.getTotalTaskCount());
        output.setCreatedAt(stage.getCreatedAt());
        output.setUpdatedAt(stage.getUpdatedAt());
        return output;
    }

    private DynamicWorkflowSummary toSummary(DynamicWorkflow workflow) {
        DynamicWorkflowSummary summary = new DynamicWorkflowSummary();
        summary.setId(workflow.getId());
        summary.setTask(workflow.getTask());
        summary.setPattern(workflow.getPattern());
        summary.setStatus(workflow.getStatus());
        summary.setProgressPercent(workflow.getProgressPercent());
        summary.setStageCount(workflow.getStageCount());
        summary.setCompletedStageCount(workflow.getCompletedStageCount());
        summary.setCreatedAt(workflow.getCreatedAt());
        return summary;
    }
}
