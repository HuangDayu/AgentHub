package com.agenthub.application.usecase;

import com.agenthub.application.dto.AgentOutput;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.application.port.out.repositories.SystemToolsRepository;
import com.agenthub.application.port.out.tools.SystemToolScannerPort;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.Agent;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.SystemTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.agenthub.domain.model.AgentConfig.Category.TOOL;
import static com.agenthub.domain.model.AgentConfig.Type.SYSTEM_TOOL;

@Component
@RequiredArgsConstructor
public class AgentUseCase {
    private final AgentRepository repository;

    public AgentOutput create(String tenantId, String workspaceId, String agentCode,
                              String name, String description) {
        Agent agent = Agent.create(tenantId, workspaceId, agentCode, name, description);
        Agent save = repository.save(agent);
        return toResult(save);
    }

    public AgentOutput get(String agentId) {
        return toResult(findById(agentId));
    }

    public List<AgentOutput> list() {
        return repository.findAll().stream().map(this::toResult).toList();
    }

    public List<AgentOutput> listByTenantAndWorkspace(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId)
                .stream().map(this::toResult).toList();
    }

    public AgentOutput update(String agentId, String name, String description) {
        Agent agent = findById(agentId);
        agent.update(name, description);
        return toResult(repository.save(agent));
    }

    public AgentOutput enabled(String agentId) {
        Agent agent = findById(agentId);
        agent.enabled();
        return toResult(repository.save(agent));
    }

    public AgentOutput unenabled(String agentId) {
        Agent agent = findById(agentId);
        agent.unenabled();
        return toResult(repository.save(agent));
    }

    public void delete(String agentId) {
        findById(agentId);
        repository.deleteById(agentId);
    }

    private Agent findById(String agentId) {
        return repository.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));
    }

    private AgentOutput toResult(Agent agent) {
        return new AgentOutput(agent.getId(), agent.getTenantId(), agent.getWorkspaceId(),
                agent.getAgentCode(), agent.getName(), agent.getDescription(),
                agent.getStatus(), agent.isEnabled(),
                agent.getCreatedAt(), agent.getUpdatedAt());
    }
}
