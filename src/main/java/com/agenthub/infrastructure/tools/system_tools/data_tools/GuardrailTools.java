package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.GuardrailStrategyRepository;
import com.agenthub.domain.model.Workspace;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentGuardrailStrategyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 护栏策略数据域工具，提供护栏策略查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "GuardrailTools", description = "护栏策略数据工具，提供护栏策略查询（已脱敏）")
public class GuardrailTools {

    private final GuardrailStrategyRepository guardrailStrategyRepository;



    private Workspace getWorkspace(ToolContext toolContext) {
        return getAgentContext(toolContext).getWorkspace().getWorkspace();
    }

    @Tool(description = "获取当前工作空间下的护栏策略列表")
    public List<AgentGuardrailStrategyDTO> getGuardrailStrategies(ToolContext toolContext) {
        return guardrailStrategyRepository.findByWorkspace(getWorkspace(toolContext).getId()).stream()
                .map(s -> BeanUtil.copyProperties(s, AgentGuardrailStrategyDTO.class))
                .collect(Collectors.toList());
    }
}
