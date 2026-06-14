package com.agenthub.infrastructure.tools.core_tools;

import com.agenthub.application.command.CreateWorkflowCommand;
import com.agenthub.application.dto.DynamicWorkflowOutput;
import com.agenthub.application.dto.DynamicWorkflowSummary;
import com.agenthub.application.usecase.DynamicWorkflowUseCase;
import com.agenthub.common.utils.TtlUtils;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.core_tools.dto.CreateWorkflowToolInput;
import com.agenthub.infrastructure.workflow.DynamicWorkflowEngine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.agenthub.infrastructure.tools.SystemToolsUtils.getAgentContext;

/**
 * 动态工作流工具，供Agent在运行时创建和管理并行子Agent编排。
 */
@RequiredArgsConstructor
@AgentTools(name = "WorkflowTools",
        description = "动态工作流工具，用于创建和管理并行子Agent编排")
public class WorkflowTools {

    private final DynamicWorkflowUseCase workflowUseCase;
    private final DynamicWorkflowEngine workflowEngine;

    @Tool(description = "创建并执行动态工作流，支持扇出、管道、评审等编排模式")
    public String createWorkflow(
            @ToolParam(description = "工作流信息") CreateWorkflowToolInput input,
            ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        CreateWorkflowCommand command = buildCommand(new CommandSpec(ctx, input.getTask(),
                input.getPattern(), input.getSubtasks()));
        DynamicWorkflowOutput workflow = workflowUseCase.createWorkflow(command);
        executeAsync(workflow.getId(), ctx);
        return "工作流已创建并开始执行: " + workflow.getId();
    }

    @Tool(description = "获取工作流执行状态和进度")
    public String getWorkflowStatus(
            @ToolParam(description = "工作流ID") String workflowId) {
        DynamicWorkflowOutput workflow = workflowUseCase.getWorkflow(workflowId);
        return formatStatus(workflow);
    }

    @Tool(description = "列出当前会话的所有工作流")
    public List<DynamicWorkflowSummary> listWorkflows(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return workflowUseCase.listBySession(ctx.getSessionId());
    }

    private CreateWorkflowCommand buildCommand(CommandSpec spec) {
        CreateWorkflowCommand command = new CreateWorkflowCommand();
        command.setAgentId(spec.getCtx().getAgent().getId());
        command.setSessionId(spec.getCtx().getSessionId());
        command.setTask(spec.getTask());
        command.setPattern(spec.getPattern());
        command.setStages(buildStages(spec.getSubtasks()));
        return command;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CommandSpec {
        private ReActAgentContext ctx;
        private String task;
        private String pattern;
        private String subtasks;
    }

    private List<CreateWorkflowCommand.StageInput> buildStages(String subtasks) {
        CreateWorkflowCommand.StageInput stage = new CreateWorkflowCommand.StageInput();
        stage.setName("执行阶段");
        stage.setStageType("FAN_OUT");
        stage.setTasks(buildTaskInputs(subtasks));
        return List.of(stage);
    }

    private List<CreateWorkflowCommand.TaskInput> buildTaskInputs(String subtasks) {
        return java.util.Arrays.stream(subtasks.split(","))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .map(this::toTaskInput)
                .toList();
    }

    private CreateWorkflowCommand.TaskInput toTaskInput(String subtask) {
        CreateWorkflowCommand.TaskInput input = new CreateWorkflowCommand.TaskInput();
        input.setTaskDescription(subtask);
        return input;
    }

    private void executeAsync(String workflowId, ReActAgentContext ctx) {
        CompletableFuture.runAsync(
                () -> workflowEngine.executeWorkflow(workflowId, ctx),
                TtlUtils.getTtlExecutorService());
    }

    private String formatStatus(DynamicWorkflowOutput workflow) {
        StringBuilder sb = new StringBuilder();
        sb.append("工作流: ").append(workflow.getId()).append("\n");
        sb.append("任务: ").append(workflow.getTask()).append("\n");
        sb.append("模式: ").append(workflow.getPattern()).append("\n");
        sb.append("状态: ").append(workflow.getStatus()).append("\n");
        sb.append("进度: ").append(workflow.getProgressPercent()).append("%\n");
        return sb.toString();
    }
}
