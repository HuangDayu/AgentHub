package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.ModelConfigRepository;
import com.agenthub.application.port.out.repositories.ModelStrategyRepository;
import com.agenthub.domain.model.Workspace;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentModelConfigDTO;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentModelStrategyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 模型数据域工具，提供模型配置与模型策略查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "ModelTools", description = "模型数据工具，提供模型配置与策略查询（已脱敏）")
public class ModelTools {

    private final ModelConfigRepository modelConfigRepository;
    private final ModelStrategyRepository modelStrategyRepository;



    private Workspace getWorkspace(ToolContext toolContext) {
        return getAgentContext(toolContext).getWorkspace().getWorkspace();
    }

    @Tool(description = "获取所有已启用的模型配置（不含API密钥等敏感信息）")
    public List<AgentModelConfigDTO> getModelConfigs() {
        return modelConfigRepository.findEnabledAll(true).stream()
                .map(c -> BeanUtil.copyProperties(c, AgentModelConfigDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取当前工作空间下的模型策略列表")
    public List<AgentModelStrategyDTO> getModelStrategies(ToolContext toolContext) {
        return modelStrategyRepository.findByWorkspace(getWorkspace(toolContext).getId()).stream()
                .map(s -> BeanUtil.copyProperties(s, AgentModelStrategyDTO.class))
                .collect(Collectors.toList());
    }
}
