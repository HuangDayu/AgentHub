package com.agenthub.application.usecase;

import com.agenthub.application.command.CreateToolCommand;
import com.agenthub.application.command.InvokeToolCommand;
import com.agenthub.application.command.UpdateToolCommand;
import com.agenthub.application.port.out.HttpToolInvoker;
import com.agenthub.application.port.out.IdempotencyCachePort;
import com.agenthub.application.port.out.repositories.ToolRepository;
import com.agenthub.common.exception.ToolNotFoundException;
import com.agenthub.domain.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工具管理用例服务。
 */
@Component
@RequiredArgsConstructor
public class ToolsUseCase {
    private static final int IDEMPOTENCY_TTL_SECONDS = 3600;
    private final ToolRepository repository;
    private final HttpToolInvoker httpToolInvoker;
    private final IdempotencyCachePort idempotencyCache;
    private final ObjectMapper objectMapper;

    public List<ToolView> listTools() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    public ToolView createTool(CreateToolCommand command) {
        Tool tool = Tool.create(ToolId.newId(), command.name(), command.description(), command.enabled());
        Tool saved = repository.save(tool);
        return toView(saved);
    }

    public ToolView getTool(String toolId) {
        return toView(requireTool(toolId));
    }

    public ToolView updateTool(String toolId, UpdateToolCommand command) {
        Tool current = requireTool(toolId);
        Tool updated = current.patch(command.name(), command.description(), command.enabled());
        Tool saved = repository.save(updated);
        return toView(saved);
    }

    public ToolInvokeView invokeTool(String toolId, InvokeToolCommand command) {
        String idempotencyKey = command.idempotencyKey();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return invokeWithIdempotency(toolId, command, idempotencyKey);
        }
        return doInvoke(toolId, command);
    }

    private ToolInvokeView invokeWithIdempotency(String toolId, InvokeToolCommand command, String key) {
        return idempotencyCache.getCachedResult(key)
                .map(this::parseCachedResult)
                .orElseGet(() -> executeAndCache(toolId, command, key));
    }

    private ToolInvokeView parseCachedResult(String cached) {
        try {
            return objectMapper.readValue(cached, ToolInvokeView.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse cached result", e);
        }
    }

    private ToolInvokeView executeAndCache(String toolId, InvokeToolCommand command, String key) {
        ToolInvokeView result = doInvoke(toolId, command);
        cacheResult(key, result);
        return result;
    }

    private void cacheResult(String key, ToolInvokeView result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            idempotencyCache.cacheResult(key, json, IDEMPOTENCY_TTL_SECONDS);
        } catch (Exception ignored) {
        }
    }

    private ToolInvokeView doInvoke(String toolId, InvokeToolCommand command) {
        ToolInvocationResult result = httpToolInvoker.invoke(toolId, command);
        return new ToolInvokeView(result.toolId(), result.status(), result.output());
    }

    private Tool requireTool(String toolId) {
        return repository.findById(ToolId.of(toolId)).orElseThrow(() -> new ToolNotFoundException(toolId));
    }

    private ToolView toView(Tool tool) {
        return new ToolView(tool.id().value(), tool.name(), tool.description(), tool.enabled());
    }
}
