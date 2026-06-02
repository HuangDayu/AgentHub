package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.TraceResponse;
import com.agenthub.application.dto.TraceOutput;
import com.agenthub.application.usecase.TraceUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Trace Controller.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/traces")
public class TraceController {
    private final TraceUseCase useCase;

    public TraceController(TraceUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{traceId}")
    public TraceResponse get(@PathVariable String traceId) {
        return toResponse(useCase.get(traceId));
    }

    @GetMapping("/runs/{runId}")
    public List<TraceResponse> listByRun(@PathVariable String runId) {
        return useCase.listByRun(runId).stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping
    public List<TraceResponse> list() {
        return useCase.list().stream()
            .map(this::toResponse)
            .toList();
    }

    @DeleteMapping("/{traceId}")
    public void delete(@PathVariable String traceId) {
        useCase.delete(traceId);
    }

    private TraceResponse toResponse(TraceOutput output) {
        return BeanUtil.copyProperties(output, TraceResponse.class);
    }
}
