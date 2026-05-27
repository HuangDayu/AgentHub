package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.RuntimeDataViewResponse;
import com.agenthub.api.dto.SpanTreeNodeResponse;
import com.agenthub.application.dto.RuntimeDataViewOutput;
import com.agenthub.application.dto.SpanTreeNodeOutput;
import com.agenthub.application.usecase.RuntimeDataViewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

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
        RuntimeDataViewResponse response = BeanUtil.copyProperties(output, RuntimeDataViewResponse.class);
        // 深度转换 span 树结构（BeanUtil.copyProperties 只做浅拷贝，嵌套对象需手动转换）
        response.setSpanTree(toSpanTreeResponse(output.getSpanTree()));
        return response;
    }

    private List<SpanTreeNodeResponse> toSpanTreeResponse(List<SpanTreeNodeOutput> nodes) {
        if (nodes == null) {
            return Collections.emptyList();
        }
        return nodes.stream()
                .map(this::toSpanTreeNodeResponse)
                .toList();
    }

    private SpanTreeNodeResponse toSpanTreeNodeResponse(SpanTreeNodeOutput node) {
        SpanTreeNodeResponse response = new SpanTreeNodeResponse();
        BeanUtil.copyProperties(node, response);
        response.setChildren(toSpanTreeResponse(node.getChildren()));
        return response;
    }
}
