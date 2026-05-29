package com.agenthub.infrastructure.tools.http_tools;

import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.AgentToolInfo;
import com.agenthub.domain.model.tools.HttpTool;
import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agenthub.domain.enums.AgentToolType.HTTP_TOOL;

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
    public AgentToolType getToolInfo() {
        return HTTP_TOOL;
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
    public Set<ToolCallback> getToolCallbacks(List<AgentToolInfo> toolIds) {
        List<String> list = toolIds.stream().map(AgentToolInfo::getConfigId).toList();
        List<HttpTool> httpTools = httpToolRepository.findByIds(list);
        return httpToolCallbackProvider.getToolCallbacks(httpTools);
    }
}
