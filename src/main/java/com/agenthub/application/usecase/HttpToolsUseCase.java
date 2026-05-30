package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.CreateHttpToolCommand;
import com.agenthub.application.command.UpdateToolCommand;
import com.agenthub.application.dto.HttpToolInvokeOutput;
import com.agenthub.application.dto.HttpToolOutput;
import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.exception.ToolNotFoundException;
import com.agenthub.domain.model.tools.HttpTool;
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


    private HttpToolInvokeOutput parseCachedResult(String cached) {
        try {
            return objectMapper.readValue(cached, HttpToolInvokeOutput.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse cached result", e);
        }
    }


    private HttpTool requireTool(String toolId) {
        return repository.findById(toolId).orElseThrow(() -> new ToolNotFoundException(toolId));
    }

    private HttpToolOutput toView(HttpTool httpTool) {
        return BeanUtil.copyProperties(httpTool, HttpToolOutput.class);
    }
}
