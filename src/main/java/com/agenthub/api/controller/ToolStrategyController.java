package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateToolStrategyRequest;
import com.agenthub.api.dto.ToolStrategyResponse;
import com.agenthub.api.dto.UpdateToolStrategyRequest;
import com.agenthub.api.dto.*;
import com.agenthub.api.mapper.ToolStrategyResponseMapper;
import com.agenthub.application.command.CreateToolStrategyCommand;
import com.agenthub.application.command.UpdateToolStrategyCommand;
import com.agenthub.application.usecase.ToolStrategyUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.agenthub.api.mapper.ToolStrategyResponseMapper.toResponse;

/**
 * 工具策略API控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/tool-strategies")
public class ToolStrategyController {
    private final ToolStrategyUseCase toolStrategyUseCase;

    public ToolStrategyController(ToolStrategyUseCase toolStrategyUseCase) {
        this.toolStrategyUseCase = toolStrategyUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ToolStrategyResponse create(
            @PathVariable String workspaceId,
            @RequestBody CreateToolStrategyRequest request
    ) {
        CreateToolStrategyCommand command = BeanUtil.copyProperties(request, CreateToolStrategyCommand.class);
        return toResponse(toolStrategyUseCase.create(command));
    }



    @GetMapping
    public List<ToolStrategyResponse> list(@PathVariable String workspaceId) {
        return toolStrategyUseCase.list(workspaceId).stream()
            .map(ToolStrategyResponseMapper::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    public ToolStrategyResponse get(
            @PathVariable String workspaceId,
            @PathVariable String id
    ) {
        return toResponse(toolStrategyUseCase.get(id));
    }

    @PutMapping("/{id}")
    public ToolStrategyResponse update(
            @PathVariable String workspaceId,
            @PathVariable String id,
            @RequestBody UpdateToolStrategyRequest request
    ) {
        UpdateToolStrategyCommand command = BeanUtil.copyProperties(request, UpdateToolStrategyCommand.class);
        return toResponse(toolStrategyUseCase.update(id, command));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String workspaceId,
            @PathVariable String id
    ) {
        toolStrategyUseCase.delete(id);
    }
}
