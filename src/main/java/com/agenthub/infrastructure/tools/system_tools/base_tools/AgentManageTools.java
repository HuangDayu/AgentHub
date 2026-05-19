package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.domain.model.agent.Agent;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "AgentManageTools", description = "智能体工具，提供智能体列表查询和计划更新功能")
public class AgentManageTools {

    private final AgentRepository agentRepository;

    @Tool(description = "获取所有智能体列表")
    public List<Agent> agentsList() {
        return agentRepository.findAll();
    }

    @Tool(description = "更新智能体计划")
    public Agent updatePlan(@ToolParam String agentId, @ToolParam String plan) {
        return agentRepository.findById(agentId).map(agent -> {
            agent.setDescription(plan);
            return agentRepository.save(agent);
        }).orElse(null);
    }

    @Tool(description = "获取智能体详情")
    public Agent agentGet(@ToolParam String agentId) {
        return agentRepository.findById(agentId).orElse(null);
    }
}
