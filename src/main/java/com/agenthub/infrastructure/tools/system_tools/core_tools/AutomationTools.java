package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.port.out.repositories.ScheduledTaskRepository;
import com.agenthub.domain.model.ScheduledTask;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.scheduler.ScheduledTaskScheduler;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.ScheduledTaskResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 自动化工具，提供定时任务的创建、查询、启停和删除功能。
 */
@RequiredArgsConstructor
@AgentTools(name = "AutomationTools", description = "自动化工具，提供定时任务的创建、查询、启停和删除功能")
public class AutomationTools {

    private final ScheduledTaskRepository scheduledTaskRepository;
    private final ScheduledTaskScheduler scheduledTaskScheduler;

    @Tool(description = "创建定时任务。返回任务ID。")
    public ScheduledTaskResult createScheduledTask(
            @ToolParam(description = "Cron表达式，如 '0 9 * * ?' 表示每天9点") String cronExpression,
            @ToolParam(description = "任务名称") String taskName,
            @ToolParam(description = "任务类型：AGENT_CHAT / WORKFLOW / SYSTEM") String taskType,
            @ToolParam(description = "执行提示词或命令内容") String prompt,
            @ToolParam(description = "执行该任务的AgentID（可选，为空则使用工作空间内第一个Agent）") String agentId,
            ToolContext toolContext) {
        CreateScheduledTaskCommand cmd = new CreateScheduledTaskCommand(
                cronExpression, taskName, taskType, prompt, agentId);
        return executeCreate(cmd, getAgentContext(toolContext));
    }

    private ScheduledTaskResult executeCreate(CreateScheduledTaskCommand cmd, ReActAgentContext ctx) {
        ScheduledTask task = newTask(cmd);
        task = buildTask(ctx, task);
        if (cmd.getAgentId() != null && !cmd.getAgentId().isBlank()) task.setAgentId(cmd.getAgentId());
        ScheduledTask saved = scheduledTaskRepository.saveOrUpdate(task);
        scheduledTaskScheduler.scheduleTask(saved);
        return toResult(saved);
    }

    private static ScheduledTask newTask(CreateScheduledTaskCommand cmd) {
        ScheduledTask task = new ScheduledTask();
        task.setName(cmd.getTaskName()); task.setTaskType(cmd.getTaskType());
        task.setCronExpression(cmd.getCronExpression()); task.setPrompt(cmd.getPrompt());
        return task;
    }

    @Tool(description = "获取当前工作空间的所有定时任务列表")
    public List<ScheduledTaskResult> listScheduledTasks(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        String workspaceId = ctx.getWorkspace().getWorkspace().getId();
        return scheduledTaskRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Tool(description = "启用定时任务")
    public ScheduledTaskResult enableScheduledTask(
            @ToolParam(description = "任务ID") String taskId) {
        ScheduledTask task = findTask(taskId);
        task.setEnabled(true);
        task.setUpdatedAt(LocalDateTime.now());
        ScheduledTask saved = scheduledTaskRepository.saveOrUpdate(task);
        scheduledTaskScheduler.rescheduleTask(saved);
        return toResult(saved);
    }

    @Tool(description = "禁用定时任务")
    public ScheduledTaskResult disableScheduledTask(
            @ToolParam(description = "任务ID") String taskId) {
        ScheduledTask task = findTask(taskId);
        task.setEnabled(false);
        task.setUpdatedAt(LocalDateTime.now());
        ScheduledTask saved = scheduledTaskRepository.saveOrUpdate(task);
        scheduledTaskScheduler.unscheduleTask(taskId);
        return toResult(saved);
    }

    @Tool(description = "删除定时任务")
    public String deleteScheduledTask(
            @ToolParam(description = "任务ID") String taskId) {
        scheduledTaskScheduler.unscheduleTask(taskId);
        scheduledTaskRepository.deleteById(taskId);
        return "定时任务已删除: " + taskId;
    }

    @Tool(description = "更新定时任务的Cron表达式")
    public ScheduledTaskResult updateScheduledTaskCron(
            @ToolParam(description = "任务ID") String taskId,
            @ToolParam(description = "新的Cron表达式") String newCronExpression) {
        ScheduledTask task = findTask(taskId);
        task.setCronExpression(newCronExpression);
        task.setUpdatedAt(LocalDateTime.now());
        ScheduledTask saved = scheduledTaskRepository.saveOrUpdate(task);
        scheduledTaskScheduler.rescheduleTask(saved);
        return toResult(saved);
    }

    @Tool(description = "立即执行一次定时任务（不等待Cron触发）")
    public String runScheduledTaskNow(
            @ToolParam(description = "任务ID") String taskId) {
        ScheduledTask task = findTask(taskId);
        scheduledTaskScheduler.scheduleTask(task);
        return "任务已触发执行: " + task.getName();
    }

    private ScheduledTask findTask(String taskId) {
        return scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new com.agenthub.domain.exception.NotFoundException(
                        "定时任务不存在: " + taskId));
    }

    private ScheduledTask buildTask(ReActAgentContext ctx, ScheduledTask task) {
        task.setTenantId(ctx.getAgent().getTenantId());
        task.setWorkspaceId(ctx.getWorkspace().getWorkspace().getId());
        task.setTaskCode(generateTaskCode(task.getName()));
        task.setEnabled(true);
        task.setStatus("ACTIVE");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private String generateTaskCode(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase() + "_" + System.currentTimeMillis();
    }

    private ScheduledTaskResult toResult(ScheduledTask task) {
        ScheduledTaskResult result = new ScheduledTaskResult();
        result.setId(task.getId());
        result.setName(task.getName());
        result.setTaskType(task.getTaskType());
        result.setCronExpression(task.getCronExpression());
        result.setPrompt(task.getPrompt());
        result.setEnabled(task.isEnabled());
        result.setStatus(task.getStatus());
        result.setScheduled(scheduledTaskScheduler.isTaskScheduled(task.getId()));
        result.setAgentId(task.getAgentId());
        result.setLastRunResult(task.getLastRunResult());
        result.setRunCount(task.getRunCount());
        return result;
    }
}
