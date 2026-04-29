package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.dto.AgentConfigOutput;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.domain.model.AgentConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentConfigUseCase {
    private final AgentConfigRepository repository;

    public AgentConfigUseCase(AgentConfigRepository repository) {
        this.repository = repository;
    }


    public AgentConfigOutput setConfig(String agentId, String category, String type,
                                       String configId, String description, Integer priority, Boolean enabled) {
        AgentConfig.Type t = AgentConfig.Type.valueOf(type);
        AgentConfig.Category c = AgentConfig.Category.valueOf(category);
        // 工具类别和知识库类别可以关联多个
        if (!AgentConfig.Category.TOOL.equals(c) && !AgentConfig.Category.KNOWLEDGE.equals(c)) {
            AgentConfig existing = repository.findOneAgentConfig(agentId, c, t);
            if (existing != null) return updateExisting(existing, configId, description, priority, enabled);
        }
        return createNew(agentId, category, type, configId, description, priority);
    }

    public List<AgentConfigOutput> listConfigs(String agentId) {
        return repository.findByAgentId(agentId).stream().map(this::toResult).toList();
    }

    public List<AgentConfigOutput> listConfigsByCategory(String agentId, String category) {
        AgentConfig.Category cat = AgentConfig.Category.valueOf(category);
        return repository.findByAgentIdAndCategory(agentId, cat).stream().map(this::toResult).toList();
    }

    public AgentConfigOutput getConfig(String id) {
        return repository.findById(id)
                .map(this::toResult)
                .orElseThrow(() -> new NotFoundException("Agent config not found: " + id));
    }

    public void deleteConfig(String id) {
        repository.deleteById(id);
    }

    public void deleteAllConfigs(String agentId) {
        repository.deleteByAgentId(agentId);
    }

    private AgentConfigOutput updateExisting(AgentConfig existing, String configId,
                                             String description, Integer priority, Boolean enabled) {
        AgentConfig updated = existing.update(configId, description, priority, enabled);
        return toResult(repository.update(updated));
    }

    private AgentConfigOutput createNew(String agentId, String category, String type,
                                        String configId, String description, Integer priority) {
        AgentConfig.Category cat = AgentConfig.Category.valueOf(category);
        AgentConfig.Type t = AgentConfig.Type.valueOf(type);
        int prio = priority != null ? priority : 0;
        AgentConfig config = AgentConfig.create(agentId, cat, t, configId, description, prio);
        return toResult(repository.save(config));
    }

    private AgentConfigOutput toResult(AgentConfig config) {
        return new AgentConfigOutput(
                config.id(), config.agentId(), config.category().name(), config.type().name(),
                config.configId(), config.description(), config.priority(), config.enabled(),
                config.createdAt(), config.updatedAt()
        );
    }
}
