package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.ScheduledTaskRepository;
import com.agenthub.application.port.out.repositories.WorkflowRepository;
import com.agenthub.domain.model.ScheduledTask;
import com.agenthub.domain.model.Workspace;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.workflow.Workflow;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentScheduledTaskDTO;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentWorkflowDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 工作流数据域工具，提供工作流与定时任务信息查询与创建（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "WorkflowTools", description = "工作流数据工具，提供工作流与定时任务查询与创建（已脱敏）")
public class WorkflowTools {

    private final WorkflowRepository workflowRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;



    private Workspace getWorkspace(ToolContext toolContext) {
        return getAgentContext(toolContext).getWorkspace().getWorkspace();
    }

    @Tool(description = "获取当前工作空间下的工作流列表")
    public List<AgentWorkflowDTO> getWorkflows(ToolContext toolContext) {
        Workspace ws = getWorkspace(toolContext);
        return workflowRepository.findByTenantIdAndWorkspaceId(ws.getTenantId(), ws.getId()).stream()
                .map(w -> BeanUtil.copyProperties(w, AgentWorkflowDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取当前工作空间下的定时任务列表（不含执行配置和提示词）")
    public List<AgentScheduledTaskDTO> getScheduledTasks(ToolContext toolContext) {
        return scheduledTaskRepository.findByWorkspaceId(getWorkspace(toolContext).getId()).stream()
                .map(t -> BeanUtil.copyProperties(t, AgentScheduledTaskDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "创建新的工作流")
    public AgentWorkflowDTO createWorkflow(ToolContext toolContext,
                                            @ToolParam(description = "工作流编码") String workflowCode,
                                            @ToolParam(description = "工作流名称") String name,
                                            @ToolParam(description = "工作流描述", required = false) String description,
                                            @ToolParam(description = "流程图定义(JSON)") String graphDefinition) {
        Workspace ws = getWorkspace(toolContext);
        Workflow workflow = Workflow.create(
                ws.getTenantId(), ws.getId(), workflowCode, name, description, graphDefinition);
        return BeanUtil.copyProperties(workflowRepository.save(workflow), AgentWorkflowDTO.class);
    }

    @Tool(description = "创建新的定时任务")
    public AgentScheduledTaskDTO createScheduledTask(ToolContext toolContext,
                                                       @ToolParam(description = "任务编码") String taskCode,
                                                       @ToolParam(description = "任务名称") String name,
                                                       @ToolParam(description = "任务类型") String taskType,
                                                       @ToolParam(description = "Cron表达式") String cronExpression,
                                                       @ToolParam(description = "执行配置(JSON)", required = false) String executorConfig,
                                                       @ToolParam(description = "执行提示词", required = false) String prompt) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        Workspace ws = ctx.getWorkspace().getWorkspace();
        ScheduledTask task = new ScheduledTask();
        task.setTenantId(ctx.getAgent().getTenantId());
        task.setWorkspaceId(ws.getId());
        task.setTaskCode(taskCode);
        task.setName(name);
        task.setTaskType(taskType);
        task.setCronExpression(cronExpression);
        task.setExecutorConfig(executorConfig);
        task.setPrompt(prompt);
        task.setEnabled(true);
        task.setStatus("ACTIVE");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return BeanUtil.copyProperties(scheduledTaskRepository.saveOrUpdate(task), AgentScheduledTaskDTO.class);
    }
}
