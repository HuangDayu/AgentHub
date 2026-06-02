package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateMetricRequest;
import com.agenthub.api.dto.MetricResponse;
import com.agenthub.application.dto.MetricOutput;
import com.agenthub.application.usecase.MetricUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Metric Controller.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/metrics")
public class MetricController {
    private final MetricUseCase useCase;

    public MetricController(MetricUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public MetricResponse create(@RequestBody CreateMetricRequest request) {
        return toResponse(useCase.create(
            request.getMetricType(),
            request.getMetricName(),
            request.getMetricValue()
        ));
    }

    @GetMapping("/runs/{runId}")
    public List<MetricResponse> listByRun(@PathVariable String runId) {
        return useCase.listByRun(runId).stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/agents/{agentId}")
    public List<MetricResponse> listByAgent(@PathVariable String agentId) {
        return useCase.listByAgent(agentId).stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/types/{metricType}")
    public List<MetricResponse> listByType(@PathVariable String metricType) {
        return useCase.listByType(metricType).stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping
    public List<MetricResponse> list() {
        return useCase.list().stream()
            .map(this::toResponse)
            .toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }

    private MetricResponse toResponse(MetricOutput output) {
        return BeanUtil.copyProperties(output, MetricResponse.class);
    }
}
