package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.AgentResponse;
import com.agenthub.api.dto.CreateAgentRequest;
import com.agenthub.application.command.CreateAgentCommand;
import com.agenthub.application.dto.AgentOutput;
import com.agenthub.application.usecase.AgentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents")
public class AgentHubController {
    private final AgentUseCase useCase;

    public AgentHubController(AgentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse create(@RequestBody CreateAgentRequest request) {
        AgentOutput result = useCase.create(BeanUtil.copyProperties(request, CreateAgentCommand.class));
        return toResponse(result);
    }

    @GetMapping
    public List<AgentResponse> list() {
        return useCase.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{agentId}")
    public AgentResponse get(@PathVariable String agentId) {
        return toResponse(useCase.get(agentId));
    }

    @PutMapping("/{agentId}")
    public AgentResponse update(@PathVariable String agentId, @RequestBody CreateAgentRequest request) {
        CreateAgentCommand command = BeanUtil.copyProperties(request, CreateAgentCommand.class);
        command.setId(agentId);
        AgentOutput result = useCase.update(command);
        return toResponse(result);
    }

    @PostMapping("/{agentId}/enabled")
    public AgentResponse enabled(@PathVariable String agentId) {
        return toResponse(useCase.enabled(agentId));
    }

    @PostMapping("/{agentId}/unenabled")
    public AgentResponse unenabled(@PathVariable String agentId) {
        return toResponse(useCase.unenabled(agentId));
    }

    @DeleteMapping("/{agentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String agentId) {
        useCase.delete(agentId);
    }

    private AgentResponse toResponse(AgentOutput result) {
        return BeanUtil.copyProperties(result, AgentResponse.class);
    }
}
