package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.command.AddStepCommand;
import com.agenthub.application.command.CreatePlanCommand;
import com.agenthub.application.command.PlanStepInput;
import com.agenthub.application.dto.ExecutionPlanOutput;
import com.agenthub.application.dto.PlanStepOutput;
import com.agenthub.application.usecase.ExecutionPlanUseCase;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.AddStepToolInput;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.PlanStepToolInput;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.UpdateStepToolInput;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 执行计划工具，供Agent在运行时自主规划和跟踪任务执行。
 */
@RequiredArgsConstructor
@AgentTools(name = "PlanTools", description = "执行计划工具，用于自主规划和跟踪任务执行流程")
public class PlanTools {

    private final ExecutionPlanUseCase executionPlanUseCase;
    private final PlanExecutor planExecutor;

    @Tool(description = "创建执行计划，定义完成目标的步骤。返回计划ID和步骤列表。")
    public ExecutionPlanOutput createPlan(
            @ToolParam(description = "执行目标") String goal,
            @ToolParam(description = "执行步骤列表") List<PlanStepToolInput> steps,
            ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        CreatePlanCommand command = buildCreateCommand(ctx, goal, steps);
        return executionPlanUseCase.createPlan(command);
    }

    @Tool(description = "获取当前会话的活跃执行计划")
    public ExecutionPlanOutput getCurrentPlan(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return executionPlanUseCase.getActivePlan(ctx.getSessionId()).orElse(null);
    }

    @Tool(description = "根据计划ID获取执行计划详情")
    public ExecutionPlanOutput getPlan(
            @ToolParam(description = "计划ID") String planId) {
        return executionPlanUseCase.getPlan(planId);
    }

    @Tool(description = "开始执行计划，将状态从PLANNING切换到EXECUTING")
    public ExecutionPlanOutput startExecution(
            @ToolParam(description = "计划ID") String planId) {
        return executionPlanUseCase.startExecution(planId);
    }

    @Tool(description = "添加步骤到现有计划")
    public ExecutionPlanOutput addStep(
            @ToolParam(description = "步骤信息") AddStepToolInput step,
            ToolContext toolContext) {
        return executionPlanUseCase.addStepToPlan(
                new AddStepCommand(step.getPlanId(), step.getDescription(),
                        step.getToolName(), step.getToolInput()));
    }

    @Tool(description = "更新步骤状态：PENDING/RUNNING/COMPLETED/FAILED/SKIPPED")
    public ExecutionPlanOutput updateStep(
            @ToolParam(description = "步骤更新信息") UpdateStepToolInput input) {
        return executionPlanUseCase.updateStep(input.getPlanId(), input.getStepId(),
                input.getStatus(), input.getOutput());
    }

    @Tool(description = "获取当前可执行的步骤列表（依赖已满足）")
    public List<PlanStepOutput> getNextSteps(
            @ToolParam(description = "计划ID") String planId) {
        return executionPlanUseCase.getNextSteps(planId);
    }

    @Tool(description = "标记计划完成，传入最终结果")
    public ExecutionPlanOutput completePlan(
            @ToolParam(description = "计划ID") String planId,
            @ToolParam(description = "最终结果") String result) {
        return executionPlanUseCase.completePlan(planId, result);
    }

    @Tool(description = "标记计划失败")
    public ExecutionPlanOutput failPlan(
            @ToolParam(description = "计划ID") String planId,
            @ToolParam(description = "失败原因") String reason) {
        return executionPlanUseCase.failPlan(planId, reason);
    }

    @Tool(description = "取消计划")
    public ExecutionPlanOutput cancelPlan(
            @ToolParam(description = "计划ID") String planId,
            @ToolParam(description = "取消原因") String reason) {
        return executionPlanUseCase.cancelPlan(planId, reason);
    }

    @Tool(description = "自动执行计划，系统自动调用各步骤的工具并更新状态")
    public String executePlan(
            @ToolParam(description = "计划ID") String planId) {
        return planExecutor.executePlan(planId);
    }

    @Tool(description = "获取计划摘要，包含步骤依赖关系可视化")
    public String getPlanSummary(
            @ToolParam(description = "计划ID") String planId) {
        ExecutionPlanOutput plan = executionPlanUseCase.getPlan(planId);
        StringBuilder sb = new StringBuilder();
        appendPlanHeader(sb, plan);
        for (PlanStepOutput step : plan.getSteps()) {
            sb.append(formatStepLine(step));
        }
        return sb.toString();
    }

    private void appendPlanHeader(StringBuilder sb, ExecutionPlanOutput plan) {
        sb.append("计划: ").append(plan.getGoal()).append("\n");
        sb.append("状态: ").append(plan.getStatus()).append("\n");
        sb.append("步骤: ").append(plan.getStepCount()).append(" 个\n");
        sb.append("完成: ").append(plan.getCompletedStepCount()).append(" 个\n\n");
        sb.append("步骤依赖图:\n");
    }

    private String formatStepLine(PlanStepOutput step) {
        String deps = step.getDependencyIds() != null && !step.getDependencyIds().isEmpty()
                ? " ← " + String.join(",", step.getDependencyIds()) : "";
        return "  [" + step.getStatus() + "] " + step.getDescription() + deps + "\n";
    }

    private CreatePlanCommand buildCreateCommand(ReActAgentContext ctx, String goal,
                                                 List<PlanStepToolInput> stepInputs) {
        CreatePlanCommand command = new CreatePlanCommand();
        command.setAgentId(ctx.getAgent().getId());
        command.setSessionId(ctx.getSessionId());
        command.setGoal(goal);
        command.setSteps(mapStepInputs(stepInputs));
        return command;
    }

    private CreatePlanCommand buildAddStepCommand(ExecutionPlanOutput plan,
                                                  List<PlanStepToolInput> newSteps) {
        CreatePlanCommand command = new CreatePlanCommand();
        command.setAgentId(plan.getAgentId());
        command.setSessionId(plan.getSessionId());
        command.setGoal(plan.getGoal());
        command.setSteps(mapStepInputs(newSteps));
        return command;
    }

    private List<PlanStepInput> mapStepInputs(List<PlanStepToolInput> toolInputs) {
        return toolInputs.stream().map(this::toDomainInput).toList();
    }

    private PlanStepInput toDomainInput(PlanStepToolInput toolInput) {
        PlanStepInput input = new PlanStepInput();
        input.setDescription(toolInput.getDescription());
        input.setToolName(toolInput.getToolName());
        input.setToolInput(toolInput.getToolInput());
        input.setDependsOn(toolInput.getDependsOn());
        return input;
    }

    private PlanStepToolInput buildStepInput(String description, String toolName, String toolInput) {
        PlanStepToolInput input = new PlanStepToolInput();
        input.setDescription(description);
        input.setToolName(toolName);
        input.setToolInput(toolInput);
        return input;
    }
}
