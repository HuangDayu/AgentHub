package com.agenthub.api.controller;

import com.agenthub.api.dto.SubagentResponse;
import com.agenthub.api.mapper.SubagentResponseMapper;
import com.agenthub.application.usecase.SubagentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 子智能体监控API控制器，仅提供查询端点。
 * Subagent 由 Agent 运行时通过 SubagentEngine 自动创建，不通过 REST API 创建。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents/{agentId}/subagents")
public class SubagentController {

    private final SubagentUseCase subagentUseCase;

    /**
     * 获取子Agent列表（监控用）。
     *
     * @param agentId 父Agent ID
     * @return 子Agent响应列表
     */
    @GetMapping
    public List<SubagentResponse> list(@PathVariable String agentId) {
        return subagentUseCase.listByParent(agentId).stream()
                .map(SubagentResponseMapper::toResponse)
                .toList();
    }

    /**
     * 获取单个子Agent（监控用）。
     *
     * @param subagentId 子Agent ID
     * @return 子Agent响应
     */
    @GetMapping("/{subagentId}")
    public SubagentResponse get(@PathVariable String subagentId) {
        return SubagentResponseMapper.toResponse(subagentUseCase.get(subagentId));
    }
}
