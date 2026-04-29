package com.agenthub.api.controller;

import com.agenthub.api.dto.AvailableConfig;
import com.agenthub.api.dto.ConfigTypeDefinition;
import com.agenthub.application.dto.AgentConfigTypeOutput;
import com.agenthub.application.dto.AvailableConfigOutput;
import com.agenthub.application.usecase.AgentConfigTypeUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agent-config-types")
public class AgentConfigTypeController {
    private final AgentConfigTypeUseCase useCase;

    public AgentConfigTypeController(AgentConfigTypeUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<ConfigTypeDefinition> getConfigTypes() {
        List<AgentConfigTypeOutput> results = useCase.getConfigTypes();
        return results.stream().map(r -> new ConfigTypeDefinition(
                r.category(),
                r.displayName(),
                r.description(),
                r.types().stream().map(t -> new ConfigTypeDefinition.TypeInfo(
                        t.type(),
                        t.displayName(),
                        t.description()
                )).toList()
        )).toList();
    }

    @GetMapping("/available")
    public List<AvailableConfig> getAvailableConfigs(
            @RequestParam String type, @RequestParam String category, @RequestParam(required = false) String workspaceId) {
        List<AvailableConfigOutput> results = useCase.getAvailableConfigs(category, type, workspaceId);
        return results.stream().map(r -> new AvailableConfig(r.id(), r.name(), r.description())).toList();
    }
}
