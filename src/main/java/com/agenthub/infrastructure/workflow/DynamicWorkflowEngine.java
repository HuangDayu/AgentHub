package com.agenthub.infrastructure.workflow;

import com.agenthub.application.command.RunSubagentCommand;
import com.agenthub.application.dto.SubagentRunOutput;
import com.agenthub.application.dto.SubagentRuntimeOutput;
import com.agenthub.application.usecase.DynamicWorkflowUseCase;
import com.agenthub.application.usecase.SubagentRuntimeUseCase;
import com.agenthub.common.utils.TtlUtils;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.workflow.AgentTask;
import com.agenthub.domain.model.workflow.DynamicWorkflow;
import com.agenthub.domain.model.workflow.WorkflowStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 动态工作流编排引擎，负责并行子Agent的调度和执行。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicWorkflowEngine {

    private final DynamicWorkflowUseCase workflowUseCase;
    private final SubagentRuntimeUseCase subagentUseCase;
    private static final int MAX_STEPS = 100;
    private static final int TASK_TIMEOUT_SECONDS = 300;

    /**
     * 执行动态工作流（同步阻塞）。
     *
     * @param workflowId 工作流ID
     * @param parentContext 父Agent上下文
     */
    public void executeWorkflow(String workflowId, ReActAgentContext parentContext) {
        DynamicWorkflow workflow = loadWorkflow(workflowId);
        workflowUseCase.updateStatus(workflowId, "EXECUTING");
        try {
            executeAllStages(workflow, parentContext);
            completeWorkflowIfSuccess(workflow);
        } catch (Exception e) {
            log.error("工作流执行失败: {}", workflowId, e);
            workflowUseCase.failWorkflow(workflowId, e.getMessage());
        }
    }

    private DynamicWorkflow loadWorkflow(String workflowId) {
        return workflowUseCase.getWorkflowDomain(workflowId);
    }

    private void executeAllStages(DynamicWorkflow workflow, ReActAgentContext parentContext) {
        int stepCount = 0;
        while (stepCount < MAX_STEPS) {
            List<WorkflowStage> executable = workflow.getExecutableStages();
            if (executable.isEmpty()) break;
            for (WorkflowStage stage : executable) {
                executeStage(workflow, stage, parentContext);
                stepCount++;
            }
        }
    }

    private void executeStage(DynamicWorkflow workflow, WorkflowStage stage, ReActAgentContext parentContext) {
        workflowUseCase.updateStageStatus(stage.getId(), "RUNNING");
        try {
            String result = dispatchStage(workflow, stage, parentContext);
            workflowUseCase.updateStageOutput(stage.getId(), result);
        } catch (Exception e) {
            log.error("阶段执行失败: {}", stage.getId(), e);
            workflowUseCase.updateStageStatus(stage.getId(), "FAILED");
        }
    }

    private String dispatchStage(DynamicWorkflow workflow, WorkflowStage stage, ReActAgentContext parentContext) {
        return switch (stage.getStageType()) {
            case "FAN_OUT" -> executeFanOutStage(workflow, stage, parentContext);
            case "SEQUENTIAL" -> executeSequentialStage(workflow, stage, parentContext);
            case "LLM_CALL" -> executeLlmCallStage(workflow, stage, parentContext);
            default -> "不支持的阶段类型: " + stage.getStageType();
        };
    }

    private String executeFanOutStage(DynamicWorkflow workflow, WorkflowStage stage, ReActAgentContext parentContext) {
        List<CompletableFuture<String>> futures = stage.getTasks().stream()
                .map(task -> CompletableFuture.supplyAsync(
                        () -> executeSingleTask(workflow, task, parentContext),
                        TtlUtils.getTtlExecutorService()))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return collectResults(futures);
    }

    private String executeSequentialStage(DynamicWorkflow workflow, WorkflowStage stage, ReActAgentContext parentContext) {
        StringBuilder accumulated = new StringBuilder();
        for (AgentTask task : stage.getTasks()) {
            String result = executeSingleTask(workflow, task, parentContext);
            accumulated.append(result).append("\n");
        }
        return accumulated.toString();
    }

    private String executeLlmCallStage(DynamicWorkflow workflow, WorkflowStage stage, ReActAgentContext parentContext) {
        if (stage.getTasks().isEmpty()) return "阶段无任务";
        AgentTask firstTask = stage.getTasks().get(0);
        return executeSingleTask(workflow, firstTask, parentContext);
    }

    private String executeSingleTask(DynamicWorkflow workflow, AgentTask task, ReActAgentContext parentContext) {
        workflowUseCase.updateTaskResult(task.getId(), "", "RUNNING");
        try {
            SubagentRunOutput output = runSubagent(workflow, task, parentContext);
            task.assignSubagent(output.getSubagentId(), output.getSubsessionId());
            SubagentRuntimeOutput result = awaitSubagent(output);
            workflowUseCase.updateTaskResult(task.getId(), result.getResult(), "COMPLETED");
            return result.getResult();
        } catch (Exception e) {
            log.error("任务执行失败: {}", task.getId(), e);
            workflowUseCase.updateTaskResult(task.getId(), e.getMessage(), "FAILED");
            return "执行失败: " + e.getMessage();
        }
    }

    private SubagentRunOutput runSubagent(DynamicWorkflow workflow, AgentTask task, ReActAgentContext parentContext) {
        RunSubagentCommand command = buildCommand(workflow, task, parentContext);
        return subagentUseCase.run(command);
    }

    private RunSubagentCommand buildCommand(DynamicWorkflow workflow, AgentTask task, ReActAgentContext parentContext) {
        RunSubagentCommand command = new RunSubagentCommand();
        command.setParentContext(parentContext);
        command.setName("workflow-" + workflow.getId() + "-task-" + task.getId());
        command.setSystemPrompt("你是一个专业的执行Agent，请完成以下任务。");
        command.setTask(task.getTaskDescription());
        command.setModelConfigId(task.getModelConfigId());
        command.setTools(task.getToolNames());
        return command;
    }

    private SubagentRuntimeOutput awaitSubagent(SubagentRunOutput output) {
        long deadline = System.currentTimeMillis() + (TASK_TIMEOUT_SECONDS * 1000L);
        while (System.currentTimeMillis() < deadline) {
            SubagentRuntimeOutput status = subagentUseCase.status(
                    output.getSubagentId(), output.getSubsessionId());
            if (isTerminal(status.getStatus())) {
                return subagentUseCase.result(
                        output.getSubagentId(), output.getSubsessionId());
            }
            sleepBriefly();
        }
        return subagentUseCase.result(output.getSubagentId(), output.getSubsessionId());
    }

    private boolean isTerminal(String status) {
        return "COMPLETED".equals(status) || "FAILED".equals(status) || "INTERRUPTED".equals(status);
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String collectResults(List<CompletableFuture<String>> futures) {
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining("\n---\n"));
    }

    private void completeWorkflowIfSuccess(DynamicWorkflow workflow) {
        if (workflow.hasFailedStages()) {
            workflowUseCase.failWorkflow(workflow.getId(), "存在失败的阶段");
        } else {
            String summary = buildCompletionSummary(workflow);
            workflowUseCase.completeWorkflow(workflow.getId(), summary);
        }
    }

    private String buildCompletionSummary(DynamicWorkflow workflow) {
        return "工作流执行完成: " + workflow.getStageCount() + " 个阶段, "
                + workflow.getCompletedStageCount() + " 个成功";
    }
}
