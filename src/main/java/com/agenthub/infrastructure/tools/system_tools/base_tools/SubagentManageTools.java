package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.application.command.RunSubagentCommand;
import com.agenthub.application.dto.SubagentMessageOutput;
import com.agenthub.application.dto.SubagentRunOutput;
import com.agenthub.application.dto.SubagentRuntimeOutput;

import java.util.List;
import com.agenthub.application.usecase.SubagentRuntimeUseCase;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.base_tools.dto.CreateSubagentToolInput;
import com.agenthub.infrastructure.tools.system_tools.base_tools.dto.SubagentHandleToolInput;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 子智能体管理工具，供Agent在运行时自主创建、控制和查询Subagent。
 */
@RequiredArgsConstructor
@AgentTools(name = "SubagentManageTools",
        description = "子智能体工具，提供子Agent的创建、控制、调度和查询功能")
public class SubagentManageTools {

    private final SubagentRuntimeUseCase subagentRuntimeUseCase;

    @Tool(description = "创建子Agent并异步执行任务。返回子Agent ID。")
    public SubagentRunOutput createSubagent(
            @ToolParam(description = "创建子Agent请求") CreateSubagentToolInput request,
            ToolContext toolContext) {
        RunSubagentCommand command = command(toolContext, request);
        return subagentRuntimeUseCase.run(command);
    }

    @Tool(description = "获取当前会话的所有子Agent列表")
    public List<SubagentRuntimeOutput> listSubagents(ToolContext toolContext) {
        return subagentRuntimeUseCase.list(getAgentContext(toolContext));
    }

    @Tool(description = "获取指定子Agent的运行状态")
    public SubagentRuntimeOutput getSubagentStatus(
            @ToolParam(description = "子Agent句柄") SubagentHandleToolInput request,
            ToolContext toolContext) {
        return subagentRuntimeUseCase.status(request.getSubagentId(), request.getSubsessionId());
    }

    @Tool(description = "停止一个正在运行的子Agent")
    public SubagentRuntimeOutput stopSubagent(
            @ToolParam(description = "子Agent句柄") SubagentHandleToolInput request,
            ToolContext toolContext) {
        return subagentRuntimeUseCase.stop(request.getSubagentId(), request.getSubsessionId());
    }

    @Tool(description = "获取子Agent的对话记录")
    public List<SubagentMessageOutput> getSubagentMessages(
            @ToolParam(description = "子Agent句柄") SubagentHandleToolInput request,
            ToolContext toolContext) {
        return subagentRuntimeUseCase.messages(request.getSubagentId(), request.getSubsessionId());
    }

    @Tool(description = "获取子Agent当前结果")
    public SubagentRuntimeOutput getSubagentResult(
            @ToolParam(description = "子Agent句柄") SubagentHandleToolInput request,
            ToolContext toolContext) {
        return subagentRuntimeUseCase.result(request.getSubagentId(), request.getSubsessionId());
    }

    private RunSubagentCommand command(ToolContext ctx, CreateSubagentToolInput request) {
        RunSubagentCommand command = commandBase(ctx, request);
        fillOptional(command, request);
        return command;
    }

    private RunSubagentCommand commandBase(ToolContext ctx, CreateSubagentToolInput request) {
        ReActAgentContext parent = getAgentContext(ctx);
        RunSubagentCommand command = new RunSubagentCommand();
        command.setParentContext(parent);
        command.setName(request.getName());
        command.setSystemPrompt(request.getSystemPrompt());
        command.setTask(request.getTask());
        return command;
    }

    private void fillOptional(RunSubagentCommand command, CreateSubagentToolInput request) {
        command.setTools(request.getTools());
        command.setKnowledgeIds(request.getKnowledgeIds());
        command.setModelConfigId(request.getModelConfigId());
    }
}
