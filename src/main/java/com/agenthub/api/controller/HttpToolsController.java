package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateToolRequest;
import com.agenthub.api.dto.HttpToolViewResponse;
import com.agenthub.api.dto.UpdateToolRequest;
import com.agenthub.api.mapper.HttpToolViewMapper;
import com.agenthub.application.command.CreateHttpToolCommand;
import com.agenthub.application.command.UpdateToolCommand;
import com.agenthub.application.usecase.HttpToolsUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.agenthub.api.mapper.HttpToolViewMapper.toResponse;

/**
 * 工具 API 控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/http-tools")
public class HttpToolsController {
    private final HttpToolsUseCase service;

    public HttpToolsController(HttpToolsUseCase service) {
        this.service = service;
    }

    @GetMapping
    public List<HttpToolViewResponse> listTools() {
        return service.listTools().stream()
                .map(HttpToolViewMapper::toResponse)
                .toList();
    }

    @PostMapping
    public HttpToolViewResponse createTool(@RequestBody CreateToolRequest request) {
        return toResponse(service.createTool(BeanUtil.copyProperties(request, CreateHttpToolCommand.class)));
    }

    @GetMapping("/{toolId}")
    public HttpToolViewResponse getTool(@PathVariable String toolId) {
        return toResponse(service.getTool(toolId));
    }

    @PatchMapping("/{toolId}")
    public HttpToolViewResponse updateTool(@PathVariable String toolId, @RequestBody UpdateToolRequest request) {
        UpdateToolCommand command = BeanUtil.copyProperties(request, UpdateToolCommand.class);
        return toResponse(service.updateTool(toolId, command));
    }

}
