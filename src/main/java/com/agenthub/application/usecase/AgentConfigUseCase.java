package com.agenthub.application.usecase;

import com.agenthub.application.dto.AgentConfigOutput;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.application.port.out.repositories.McpToolRepository;
import com.agenthub.application.port.out.repositories.SystemToolsRepository;
import com.agenthub.application.port.out.tools.SystemToolScannerPort;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.McpTool;
import com.agenthub.domain.model.SystemTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.agenthub.domain.model.AgentConfig.Category.TOOL;
import static com.agenthub.domain.model.AgentConfig.Type.MCP_TOOL;
import static com.agenthub.domain.model.AgentConfig.Type.SYSTEM_TOOL;

@Component
@RequiredArgsConstructor
public class AgentConfigUseCase {
    private final AgentConfigRepository agentConfigRepository;
    private final SystemToolScannerPort systemToolScannerPort;
    private final SystemToolsRepository systemToolsRepository;
    private final McpToolRepository mcpToolRepository;

    public void syncConfig(String agentId) {
        syncSystemTool(agentId);
        syncMcpTool(agentId);
        syncSkillTool(agentId);
    }

    private void syncSkillTool(String agentId) {

    }

    private void syncMcpTool(String agentId) {
        List<McpTool> list = mcpToolRepository.findList();
        for (McpTool mcpTool : list) {
            agentConfigRepository.save(AgentConfig.create(agentId, TOOL, MCP_TOOL, mcpTool.id(), mcpTool.name(), 1, mcpTool.enabled()));
        }
    }

    public void syncSystemTool(String agentId) {
        List<SystemTool> systemTools = systemToolScannerPort.scanSystemTools();
        systemTools = systemToolsRepository.syncTools(systemTools);
        for (SystemTool systemTool : systemTools) {
            agentConfigRepository.save(AgentConfig.create(agentId, TOOL, SYSTEM_TOOL, systemTool.getId(), systemTool.getToolName(), 1, systemTool.isEnabled()));
        }
    }

    public AgentConfigOutput setConfig(String agentId, String category, String type,
                                       String configId, String description, Integer priority, Boolean enabled) {
        AgentConfig.Type t = AgentConfig.Type.valueOf(type);
        AgentConfig.Category c = AgentConfig.Category.valueOf(category);
        // 工具类别和知识库类别可以关联多个
        if (!AgentConfig.Category.TOOL.equals(c) && !AgentConfig.Category.KNOWLEDGE.equals(c)) {
            AgentConfig existing = agentConfigRepository.findOneAgentConfig(agentId, c, t);
            if (existing != null) return updateExisting(existing, configId, description, priority, enabled);
        }
        return createNew(agentId, category, type, configId, description, priority);
    }

    public AgentConfigOutput updateConfig(String id, String agentId, String category, String type,
                                          String configId, String description, Integer priority, Boolean enabled) {
        AgentConfig existing = agentConfigRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agent config not found: " + id));
        return updateExisting(existing, configId, description, priority, enabled);
    }

    public List<AgentConfigOutput> listConfigs(String agentId) {
        return agentConfigRepository.findByAgentId(agentId).stream().map(this::toResult).toList();
    }

    public List<AgentConfigOutput> listConfigsByCategory(String agentId, String category) {
        AgentConfig.Category cat = AgentConfig.Category.valueOf(category);
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

    private AgentConfigOutput updateExisting(AgentConfig existing, String configId,
                                             String description, Integer priority, Boolean enabled) {
        AgentConfig updated = existing.update(configId, description, priority, enabled);
        return toResult(agentConfigRepository.update(updated));
    }

    private AgentConfigOutput createNew(String agentId, String category, String type,
                                        String configId, String description, Integer priority) {
        AgentConfig.Category cat = AgentConfig.Category.valueOf(category);
        AgentConfig.Type t = AgentConfig.Type.valueOf(type);
        int prio = priority != null ? priority : 0;
        AgentConfig config = AgentConfig.create(agentId, cat, t, configId, description, prio, true);
        return toResult(agentConfigRepository.save(config));
    }

    private AgentConfigOutput toResult(AgentConfig config) {
        return new AgentConfigOutput(
                config.id(), config.agentId(), config.category().name(), config.type().name(),
                config.configId(), config.description(), config.priority(), config.enabled(),
                config.createdAt(), config.updatedAt()
        );
    }
}
