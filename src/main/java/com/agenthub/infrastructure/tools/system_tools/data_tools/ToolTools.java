package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.model.Workspace;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;

/**
 * 工具数据域工具，提供HTTP工具、MCP工具、系统工具、工具策略查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "ToolTools", description = "工具数据工具，提供各类工具与工具策略查询（已脱敏）")
public class ToolTools {

    private final HttpToolRepository httpToolRepository;
    private final McpToolRepository mcpToolRepository;
    private final SystemToolsRepository systemToolsRepository;
    private final ToolStrategyRepository toolStrategyRepository;

    private ReActAgentContext getAgentContext(ToolContext toolContext) {
        return (ReActAgentContext) toolContext.getContext().get(AGENT_CONTEXT_KEY);
    }

    private Workspace getWorkspace(ToolContext toolContext) {
        return getAgentContext(toolContext).getWorkspace().getWorkspace();
    }

    @Tool(description = "获取当前工作空间下可用的HTTP工具列表")
    public List<AgentHttpToolDTO> getHttpTools(ToolContext toolContext) {
        return httpToolRepository.findByWorkspaceId(getWorkspace(toolContext).getId()).stream()
                .map(t -> BeanUtil.copyProperties(t, AgentHttpToolDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取当前工作空间下可用的MCP工具列表（不含服务器地址、命令等敏感配置）")
    public List<AgentMcpToolDTO> getMcpTools(ToolContext toolContext) {
        return mcpToolRepository.findByWorkspaceId(getWorkspace(toolContext).getId()).stream()
                .map(t -> BeanUtil.copyProperties(t, AgentMcpToolDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取当前工作空间下的系统工具列表")
    public List<AgentSystemToolDTO> getSystemTools(ToolContext toolContext) {
        return systemToolsRepository.findByWorkspaceId(getWorkspace(toolContext).getId()).stream()
                .map(t -> BeanUtil.copyProperties(t, AgentSystemToolDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取当前工作空间下的工具策略列表")
    public List<AgentToolStrategyDTO> getToolStrategies(ToolContext toolContext) {
        return toolStrategyRepository.findByWorkspace(getWorkspace(toolContext).getId()).stream()
                .map(s -> BeanUtil.copyProperties(s, AgentToolStrategyDTO.class))
                .collect(Collectors.toList());
    }
}
