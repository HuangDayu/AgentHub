package com.agenthub.api.controller;

import com.agenthub.api.dto.AgentConfigResponse;
import com.agenthub.api.dto.SetAgentConfigRequest;
import com.agenthub.api.dto.*;
import com.agenthub.application.dto.AgentConfigOutput;
import com.agenthub.application.usecase.AgentConfigUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents/{agentId}/configs")
public class AgentConfigController {
    private final AgentConfigUseCase useCase;

    public AgentConfigController(AgentConfigUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentConfigResponse setConfig(@PathVariable String agentId,
                                         @RequestBody SetAgentConfigRequest request) {
        AgentConfigOutput result = useCase.setConfig(agentId, request.category(), request.type(),
                request.configId(), request.description(), request.priority(), request.enabled());
        return toResponse(result);
    }

    @PutMapping("/{id}")
    public AgentConfigResponse updateConfig(@PathVariable String agentId, @PathVariable String id,
                                            @RequestBody SetAgentConfigRequest request) {
        AgentConfigOutput result = useCase.updateConfig(id, agentId, request.category(), request.type(),
                request.configId(), request.description(), request.priority(), request.enabled());
        return toResponse(result);
    }

    @GetMapping
    public List<AgentConfigResponse> listConfigs(@PathVariable String agentId,
                                                  @RequestParam(required = false) String category) {
        List<AgentConfigOutput> results = category != null
                ? useCase.listConfigsByCategory(agentId, category)
                : useCase.listConfigs(agentId);
        return results.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AgentConfigResponse getConfig(@PathVariable String agentId, @PathVariable String id) {
        return toResponse(useCase.getConfig(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConfig(@PathVariable String agentId, @PathVariable String id) {
        useCase.deleteConfig(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllConfigs(@PathVariable String agentId) {
        useCase.deleteAllConfigs(agentId);
    }

    private AgentConfigResponse toResponse(AgentConfigOutput result) {
        return new AgentConfigResponse(result.id(), result.agentId(), result.category(), result.type(),
                result.configId(), result.description(), result.priority(), result.enabled(),
                result.createdAt(), result.updatedAt());
    }
}
