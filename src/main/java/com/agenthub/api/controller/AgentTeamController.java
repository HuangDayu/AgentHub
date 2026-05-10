package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.AgentTeamResponse;
import com.agenthub.api.dto.CreateAgentTeamRequest;
import com.agenthub.application.command.AgentTeamCommand;
import com.agenthub.application.dto.AgentTeamOutput;
import com.agenthub.application.usecase.AgentTeamUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/teams")
public class AgentTeamController {
    private final AgentTeamUseCase useCase;

    public AgentTeamController(AgentTeamUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentTeamResponse create(@RequestBody CreateAgentTeamRequest request) {
        AgentTeamOutput result = useCase.create(BeanUtil.copyProperties(request, AgentTeamCommand.class));
        return toResponse(result);
    }

    @GetMapping
    public List<AgentTeamResponse> list() {
        return useCase.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{teamId}")
    public AgentTeamResponse get(@PathVariable String teamId) {
        return toResponse(useCase.get(teamId));
    }

    @PutMapping("/{teamId}")
    public AgentTeamResponse update(@PathVariable String teamId,
                                    @RequestBody CreateAgentTeamRequest request) {
        AgentTeamCommand command = BeanUtil.copyProperties(request, AgentTeamCommand.class);
        command.setTenantId(teamId);
        AgentTeamOutput result = useCase.update(command);
        return toResponse(result);
    }

    @PostMapping("/{teamId}/activate")
    public AgentTeamResponse activate(@PathVariable String teamId) {
        return toResponse(useCase.activate(teamId));
    }

    @PostMapping("/{teamId}/deactivate")
    public AgentTeamResponse deactivate(@PathVariable String teamId) {
        return toResponse(useCase.deactivate(teamId));
    }

    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String teamId) {
        useCase.delete(teamId);
    }

    private AgentTeamResponse toResponse(AgentTeamOutput result) {
        return BeanUtil.copyProperties(result, AgentTeamResponse.class);
    }
}
