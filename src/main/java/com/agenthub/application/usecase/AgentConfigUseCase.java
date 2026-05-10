package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.AgentConfigCommand;
import com.agenthub.application.dto.AgentConfigOutput;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.application.port.out.repositories.McpToolRepository;
import com.agenthub.application.port.out.repositories.SystemToolsRepository;
import com.agenthub.application.port.out.tools.SkillToolScannerPort;
import com.agenthub.application.port.out.tools.SystemToolScannerPort;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.agenthub.common.utils.TtlUtils.parallelStreamWithTtl;
import static com.agenthub.domain.model.AgentConfigCategory.TOOL;
import static com.agenthub.domain.model.AgentConfigType.*;

@Component
@RequiredArgsConstructor
public class AgentConfigUseCase {
    private final AgentConfigRepository agentConfigRepository;
    private final SystemToolScannerPort systemToolScannerPort;
    private final SystemToolsRepository systemToolsRepository;
    private final McpToolRepository mcpToolRepository;
    private final SkillToolScannerPort skillToolScannerPort;

    @Value("${agenthub.skills.share-path:${user.home}/.agents/skills}")
    private String skillSharePath;

    public void syncConfig(String agentId) {
        syncSystemTool(agentId);
        syncMcpTool(agentId);
        syncSkillTool(agentId);
    }

    private void syncSkillTool(String agentId) {
        List<Skill> skills = skillToolScannerPort.scanSkills(skillSharePath);
        parallelStreamWithTtl(4, skills, skill -> {
            agentConfigRepository.saveOrUpdate(new AgentConfig(agentId, TOOL, SKILL_TOOL, skill.getSkillCode(), skill.getName(), skill.getDescription(), 1, skill.isEnabled()));
            return null;
        });
    }

    private void syncMcpTool(String agentId) {
        List<McpTool> list = mcpToolRepository.findList();
        parallelStreamWithTtl(4, list, mcpTool -> {
            agentConfigRepository.saveOrUpdate(new AgentConfig(agentId, TOOL, MCP_TOOL, mcpTool.getId(), mcpTool.getName(), mcpTool.getDescription(), 1, mcpTool.isEnabled()));
            return null;
        });
    }

    public void syncSystemTool(String agentId) {
        List<SystemTool> systemTools = systemToolScannerPort.scanSystemTools();
        systemTools = systemToolsRepository.syncTools(systemTools);
        parallelStreamWithTtl(4, systemTools, systemTool -> {
            agentConfigRepository.saveOrUpdate(new AgentConfig(agentId, TOOL, SYSTEM_TOOL, systemTool.getId(), systemTool.getToolName(), systemTool.getDescription(), 1, systemTool.isEnabled()));
            return null;
        });
    }

    public AgentConfigOutput saveOrUpdateConfig(AgentConfigCommand command) {
        AgentConfigType type = AgentConfigType.valueOf(command.getType());
        AgentConfigCategory category = AgentConfigCategory.valueOf(command.getCategory());
        AgentConfig agentConfig = BeanUtil.copyProperties(command, AgentConfig.class);
        agentConfig.setType(type);
        agentConfig.setCategory(category);
        AgentConfig agentConfig1 = agentConfigRepository.saveOrUpdate(agentConfig);
        return toResult(agentConfig1);
    }

    public List<AgentConfigOutput> listConfigs(String agentId) {
        return agentConfigRepository.findByAgentId(agentId).stream().map(this::toResult).toList();
    }

    public List<AgentConfigOutput> listConfigsByCategory(String agentId, String category) {
        AgentConfigCategory cat = AgentConfigCategory.valueOf(category);
        return agentConfigRepository.findByAgentIdAndCategory(agentId, cat).stream().map(this::toResult).toList();
    }

    public AgentConfigOutput getConfig(String id) {
        return agentConfigRepository.findById(id)
                .map(this::toResult)
                .orElseThrow(() -> new NotFoundException("Agent config not found: " + id));
    }

    public void deleteConfig(String id) {
        agentConfigRepository.deleteById(id);
    }

    public void deleteAllConfigs(String agentId) {
        agentConfigRepository.deleteByAgentId(agentId);
    }

    private AgentConfigOutput toResult(AgentConfig config) {
        return BeanUtil.copyProperties(config, AgentConfigOutput.class);
    }
}
