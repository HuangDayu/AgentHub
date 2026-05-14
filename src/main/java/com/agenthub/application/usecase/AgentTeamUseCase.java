package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.AgentTeamCommand;
import com.agenthub.application.dto.AgentTeamOutput;
import com.agenthub.application.port.out.repositories.AgentTeamRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.AgentTeam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentTeamUseCase {
    private final AgentTeamRepository repository;

    public AgentTeamOutput create(AgentTeamCommand command) {
        AgentTeam team = BeanUtil.copyProperties(command, AgentTeam.class);
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

    public AgentTeamOutput update(AgentTeamCommand command) {
        AgentTeam team = BeanUtil.copyProperties(command, AgentTeam.class);
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
