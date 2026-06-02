package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.SpanResponse;
import com.agenthub.application.dto.SpanOutput;
import com.agenthub.application.usecase.SpanUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Span Controller.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/spans")
public class SpanController {
    private final SpanUseCase useCase;

    public SpanController(SpanUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{spanId}")
    public SpanResponse get(@PathVariable String spanId) {
        return toResponse(useCase.get(spanId));
    }

    @GetMapping("/traces/{traceId}")
    public List<SpanResponse> listByTrace(@PathVariable String traceId) {
        return useCase.listByTrace(traceId).stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/runs/{runId}")
    public List<SpanResponse> listByRun(@PathVariable String runId) {
        return useCase.listByRun(runId).stream()
            .map(this::toResponse)
            .toList();
    }


    @DeleteMapping("/{spanId}")
    public void delete(@PathVariable String spanId) {
        useCase.delete(spanId);
    }

    private SpanResponse toResponse(SpanOutput output) {
        return BeanUtil.copyProperties(output, SpanResponse.class);
    }
}
