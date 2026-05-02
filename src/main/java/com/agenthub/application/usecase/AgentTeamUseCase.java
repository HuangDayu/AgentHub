package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.dto.AgentTeamOutput;
import com.agenthub.application.port.out.repositories.AgentTeamRepository;
import com.agenthub.domain.model.AgentTeam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentTeamUseCase {
    private final AgentTeamRepository repository;

    public AgentTeamOutput create(String tenantId, String workspaceId, String teamCode,
                                  String name, String description, String coordinationMode,
                                  String memberConfig) {
        AgentTeam team = AgentTeam.create(tenantId, workspaceId, teamCode,
                name, description, coordinationMode, memberConfig);
        return toOutput(repository.save(team));
    }

    public AgentTeamOutput get(String teamId) {
        return toOutput(findById(teamId));
    }

    public List<AgentTeamOutput> list() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    public List<AgentTeamOutput> listByTenantAndWorkspace(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId)
                .stream().map(this::toOutput).toList();
    }

    public AgentTeamOutput update(String teamId, String name, String description,
                                  String coordinationMode, String memberConfig) {
        AgentTeam team = findById(teamId);
        team.update(name, description, coordinationMode, memberConfig);
        return toOutput(repository.save(team));
    }

    public AgentTeamOutput activate(String teamId) {
        AgentTeam team = findById(teamId);
        team.activate();
        return toOutput(repository.save(team));
    }

    public AgentTeamOutput deactivate(String teamId) {
        AgentTeam team = findById(teamId);
        team.deactivate();
        return toOutput(repository.save(team));
    }

    public void delete(String teamId) {
        findById(teamId);
        repository.deleteById(teamId);
    }

    private AgentTeam findById(String teamId) {
        return repository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("AgentTeam not found: " + teamId));
    }

    private AgentTeamOutput toOutput(AgentTeam team) {
        return new AgentTeamOutput(team.getId(), team.getTenantId(), team.getWorkspaceId(),
                team.getTeamCode(), team.getName(), team.getDescription(),
                team.getCoordinationMode(), team.getMemberConfig(), team.getStatus(),
                team.getCreatedAt(), team.getUpdatedAt());
    }
}
