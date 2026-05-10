package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.CreateHttpToolCommand;
import com.agenthub.application.command.InvokeToolCommand;
import com.agenthub.application.command.UpdateToolCommand;
import com.agenthub.application.port.out.HttpToolInvoker;
import com.agenthub.application.port.out.IdempotencyCachePort;
import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.common.exception.ToolNotFoundException;
import com.agenthub.domain.model.HttpTool;
import com.agenthub.domain.model.HttpToolInvocationResult;
import com.agenthub.domain.model.HttpToolInvokeView;
import com.agenthub.domain.model.HttpToolView;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工具管理用例服务。
 */
@Component
@RequiredArgsConstructor
public class HttpToolsUseCase {
    private static final int IDEMPOTENCY_TTL_SECONDS = 3600;
    private final HttpToolRepository repository;
    private final HttpToolInvoker httpToolInvoker;
    private final IdempotencyCachePort idempotencyCache;
    private final ObjectMapper objectMapper;

    public List<HttpToolView> listTools() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    public HttpToolView createTool(CreateHttpToolCommand command) {
        HttpTool httpTool = BeanUtil.copyProperties(command, HttpTool.class);
        HttpTool saved = repository.save(httpTool);
        return toView(saved);
    }

    public HttpToolView getTool(String toolId) {
        return toView(requireTool(toolId));
    }

    public HttpToolView updateTool(String toolId, UpdateToolCommand command) {
        HttpTool updated = BeanUtil.copyProperties(command, HttpTool.class);
        updated.setId(toolId);
        HttpTool saved = repository.save(updated);
        return toView(saved);
    }

    public HttpToolInvokeView invokeTool(String toolId, InvokeToolCommand command) {
        String idempotencyKey = command.getIdempotencyKey();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return invokeWithIdempotency(toolId, command, idempotencyKey);
        }
        return doInvoke(toolId, command);
    }

    private HttpToolInvokeView invokeWithIdempotency(String toolId, InvokeToolCommand command, String key) {
        return idempotencyCache.getCachedResult(key)
                .map(this::parseCachedResult)
                .orElseGet(() -> executeAndCache(toolId, command, key));
    }

    private HttpToolInvokeView parseCachedResult(String cached) {
        try {
            return objectMapper.readValue(cached, HttpToolInvokeView.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse cached result", e);
        }
    }

    private HttpToolInvokeView executeAndCache(String toolId, InvokeToolCommand command, String key) {
        HttpToolInvokeView result = doInvoke(toolId, command);
        cacheResult(key, result);
        return result;
    }

    private void cacheResult(String key, HttpToolInvokeView result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            idempotencyCache.cacheResult(key, json, IDEMPOTENCY_TTL_SECONDS);
        } catch (Exception ignored) {
        }
    }

    private HttpToolInvokeView doInvoke(String toolId, InvokeToolCommand command) {
        HttpToolInvocationResult result = httpToolInvoker.invoke(toolId, command);
        return BeanUtil.copyProperties(result, HttpToolInvokeView.class);
    }

    private HttpTool requireTool(String toolId) {
        return repository.findById(toolId).orElseThrow(() -> new ToolNotFoundException(toolId));
    }

    private HttpToolView toView(HttpTool httpTool) {
        return BeanUtil.copyProperties(httpTool, HttpToolView.class);
    }
}
