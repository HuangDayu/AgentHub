package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentConfigDTO;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * Agent自身数据域工具，提供Agent身份与配置信息查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "AgentInfoTools", description = "Agent自身信息工具，提供Agent身份与配置查询（已脱敏）")
public class AgentInfoTools {

    private final AgentConfigRepository agentConfigRepository;



    @Tool(description = "获取当前Agent自身信息（不含租户、工作空间等内部ID）")
    public AgentInfoDTO getAgentInfo(ToolContext toolContext) {
        return BeanUtil.copyProperties(getAgentContext(toolContext).getAgent(), AgentInfoDTO.class);
    }

    @Tool(description = "获取当前Agent的配置关联列表")
    public List<AgentConfigDTO> getAgentConfigs(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return agentConfigRepository.findByAgentId(ctx.getAgent().getId()).stream()
                .map(c -> BeanUtil.copyProperties(c, AgentConfigDTO.class))
                .collect(Collectors.toList());
    }
}
