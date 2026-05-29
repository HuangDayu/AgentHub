package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.TenantRepository;
import com.agenthub.application.port.out.repositories.WorkspaceRepository;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentTenantDTO;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentWorkspaceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 工作空间数据域工具，提供工作空间与租户信息查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "WorkspaceTools", description = "工作空间数据工具，提供工作空间与租户信息查询（已脱敏）")
public class WorkspaceTools {

    private final WorkspaceRepository workspaceRepository;
    private final TenantRepository tenantRepository;



    @Tool(description = "获取Agent当前所在的工作空间信息")
    public AgentWorkspaceDTO getWorkspace(ToolContext toolContext) {
        return BeanUtil.copyProperties(getAgentContext(toolContext).getWorkspace().getWorkspace(), AgentWorkspaceDTO.class);
    }

    @Tool(description = "获取当前Agent所属租户信息")
    public AgentTenantDTO getTenant(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return tenantRepository.findById(ctx.getAgent().getTenantId())
                .map(t -> BeanUtil.copyProperties(t, AgentTenantDTO.class))
                .orElse(null);
    }
}
