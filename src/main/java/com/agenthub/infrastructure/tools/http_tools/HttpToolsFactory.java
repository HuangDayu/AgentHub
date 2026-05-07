package com.agenthub.infrastructure.tools.http_tools;

import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.model.AgentToolInfo;
import com.agenthub.domain.model.HttpTool;
import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agenthub.domain.model.AgentToolType.HTTP_TOOLS;

/**
 * HTTP工具工厂，负责提供HTTP工具的ToolCallback。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class HttpToolsFactory implements AbstractToolsFactory {

    private final HttpToolCallbackProvider httpToolCallbackProvider;
    private final HttpToolRepository httpToolRepository;

    @Override
    public AgentToolInfo getToolInfo() {
        return new AgentToolInfo(HTTP_TOOLS);
    }

    @Override
    public Set<ToolCallback> getAllToolCallbacks() {
        return httpToolCallbackProvider.getToolCallbacks(httpToolRepository.findAll());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(String name) {
        return getAllToolCallbacks().stream()
                .filter(toolCallback -> toolCallback.getToolDefinition().name().equals(name))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(List<String> toolIds) {
        List<HttpTool> httpTools = httpToolRepository.findByIds(toolIds);
        return httpToolCallbackProvider.getToolCallbacks(httpTools);
    }
}
