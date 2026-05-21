package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.AgentTeamRepository;
import com.agenthub.domain.model.Workspace;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentTeamDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;

/**
 * 团队数据域工具，提供Agent团队信息查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "TeamTools", description = "团队数据工具，提供Agent团队信息查询（已脱敏）")
public class TeamTools {

    private final AgentTeamRepository agentTeamRepository;

    private ReActAgentContext getAgentContext(ToolContext toolContext) {
        return (ReActAgentContext) toolContext.getContext().get(AGENT_CONTEXT_KEY);
    }

    private Workspace getWorkspace(ToolContext toolContext) {
        return getAgentContext(toolContext).getWorkspace().getWorkspace();
    }

    @Tool(description = "获取当前工作空间下的Agent团队列表")
    public List<AgentTeamDTO> getTeams(ToolContext toolContext) {
        Workspace ws = getWorkspace(toolContext);
        return agentTeamRepository.findByTenantIdAndWorkspaceId(ws.getTenantId(), ws.getId()).stream()
                .map(t -> BeanUtil.copyProperties(t, AgentTeamDTO.class))
                .collect(Collectors.toList());
    }
}
