package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.AlertResponse;
import com.agenthub.api.dto.CreateAlertRequest;
import com.agenthub.application.command.CreateAlertCommand;
import com.agenthub.application.dto.AlertOutput;
import com.agenthub.application.usecase.AlertUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Alert Controller.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/alerts")
public class AlertController {
    private final AlertUseCase useCase;

    public AlertController(AlertUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public AlertResponse create(@RequestBody CreateAlertRequest request) {
        return toResponse(useCase.create(new CreateAlertCommand(
            request.getAlertLevel(),
            request.getAlertType(),
            request.getTitle(),
            request.getMessage()
        )));
    }

    @GetMapping("/{id}")
    public AlertResponse get(@PathVariable String id) {
        return toResponse(useCase.get(id));
    }

    @PutMapping("/{id}/resolve")
    public AlertResponse resolve(
        @PathVariable String id,
        @RequestParam String resolvedBy
    ) {
        return toResponse(useCase.resolve(id, resolvedBy));
    }

    @GetMapping("/runs/{runId}")
    public List<AlertResponse> listByRun(@PathVariable String runId) {
        return useCase.listByRun(runId).stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/unresolved")
    public List<AlertResponse> listUnresolved() {
        return useCase.listUnresolved().stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping
    public List<AlertResponse> list() {
        return useCase.list().stream()
            .map(this::toResponse)
            .toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }

    private AlertResponse toResponse(AlertOutput output) {
        return BeanUtil.copyProperties(output, AlertResponse.class);
    }
}
