package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.CreateHttpToolCommand;
import com.agenthub.application.command.InvokeToolCommand;
import com.agenthub.application.command.UpdateToolCommand;
import com.agenthub.application.port.out.HttpToolInvoker;
import com.agenthub.application.port.out.IdempotencyCachePort;
import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.exception.ToolNotFoundException;
import com.agenthub.domain.model.HttpTool;
import com.agenthub.domain.model.HttpToolInvokeResult;
import com.agenthub.application.dto.HttpToolInvokeOutput;
import com.agenthub.application.dto.HttpToolOutput;
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

    public List<HttpToolOutput> listTools() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    public HttpToolOutput createTool(CreateHttpToolCommand command) {
        HttpTool httpTool = BeanUtil.copyProperties(command, HttpTool.class);
        HttpTool saved = repository.save(httpTool);
        return toView(saved);
    }

    public HttpToolOutput getTool(String toolId) {
        return toView(requireTool(toolId));
    }

    public HttpToolOutput updateTool(String toolId, UpdateToolCommand command) {
        HttpTool existing = requireTool(toolId);
        if (command.getName() != null) existing.setName(command.getName());
        if (command.getDescription() != null) existing.setDescription(command.getDescription());
        if (command.getEnabled() != null) existing.setEnabled(command.getEnabled());
        // Update existing tool
        HttpTool saved = repository.save(existing);
        return toView(saved);
    }

    public HttpToolInvokeOutput invokeTool(String toolId, InvokeToolCommand command) {
        String idempotencyKey = command.getIdempotencyKey();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return invokeWithIdempotency(toolId, command, idempotencyKey);
        }
        return doInvoke(toolId, command);
    }

    private HttpToolInvokeOutput invokeWithIdempotency(String toolId, InvokeToolCommand command, String key) {
        return idempotencyCache.getCachedResult(key)
                .map(this::parseCachedResult)
                .orElseGet(() -> executeAndCache(toolId, command, key));
    }

    private HttpToolInvokeOutput parseCachedResult(String cached) {
        try {
            return objectMapper.readValue(cached, HttpToolInvokeOutput.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse cached result", e);
        }
    }

    private HttpToolInvokeOutput executeAndCache(String toolId, InvokeToolCommand command, String key) {
        HttpToolInvokeOutput result = doInvoke(toolId, command);
        cacheResult(key, result);
        return result;
    }

    private void cacheResult(String key, HttpToolInvokeOutput result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            idempotencyCache.cacheResult(key, json, IDEMPOTENCY_TTL_SECONDS);
        } catch (Exception ignored) {
        }
    }

    private HttpToolInvokeOutput doInvoke(String toolId, InvokeToolCommand command) {
        HttpToolInvokeResult result = httpToolInvoker.invoke(toolId, command);
        return BeanUtil.copyProperties(result, HttpToolInvokeOutput.class);
    }

    private HttpTool requireTool(String toolId) {
        return repository.findById(toolId).orElseThrow(() -> new ToolNotFoundException(toolId));
    }

    private HttpToolOutput toView(HttpTool httpTool) {
        return BeanUtil.copyProperties(httpTool, HttpToolOutput.class);
    }
}
