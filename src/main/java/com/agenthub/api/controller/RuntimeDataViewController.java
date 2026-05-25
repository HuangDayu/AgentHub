package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.RuntimeDataViewResponse;
import com.agenthub.application.dto.RuntimeDataViewOutput;
import com.agenthub.application.usecase.RuntimeDataViewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions")
public class RuntimeDataViewController {
    private final RuntimeDataViewUseCase useCase;

    @GetMapping("/{sessionId}/data-view")
    public RuntimeDataViewResponse get(@PathVariable String agentId, @PathVariable String sessionId) {
        RuntimeDataViewOutput output = useCase.get(agentId, sessionId);
        return toResponse(output);
    }

    private RuntimeDataViewResponse toResponse(RuntimeDataViewOutput output) {
        return BeanUtil.copyProperties(output, RuntimeDataViewResponse.class);
    }
}
