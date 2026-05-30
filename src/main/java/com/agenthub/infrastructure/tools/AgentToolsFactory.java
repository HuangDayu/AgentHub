package com.agenthub.infrastructure.tools;

import com.agenthub.application.port.out.tools.ToolCallbackResolverPort;
import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.AgentToolInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具工厂，负责根据工具类型和配置解析为可执行的工具回调。
 */
@RequiredArgsConstructor
@Component
public class AgentToolsFactory implements ToolCallbackResolverPort {

    private final List<AbstractToolsFactory> abstractToolsFactory;
    private final Map<AgentToolType, AbstractToolsFactory> toolTypeMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        abstractToolsFactory.forEach(factory -> toolTypeMap.put(factory.getToolInfo(), factory));
    }

    public Set<ToolCallback> getToolCallback(AgentToolType toolType, String toolName) {
        AbstractToolsFactory factory = toolTypeMap.get(toolType);
        if (factory == null) {
            return Set.of();
        }
        return factory.getToolCallbacks(toolName);
    }

    public Set<ToolCallback> getToolCallbacks(AgentToolType toolType, List<AgentToolInfo> toolIds) {
        AbstractToolsFactory factory = toolTypeMap.get(toolType);
        if (toolIds == null || toolIds.isEmpty() || factory == null) {
            return Set.of();
        }
        return factory.getToolCallbacks(toolIds.stream()
                .filter(toolInfo -> toolInfo.getType() == toolType).toList());
    }

    @Override
    public Set<Object> resolveToolCallbacks(AgentToolType toolType, List<AgentToolInfo> toolIds) {
        Set<ToolCallback> callbacks = getToolCallbacks(toolType, toolIds);
        return Set.copyOf(callbacks);
    }

    @Override
    public java.util.Optional<Object> resolveByName(String toolName) {
        for (AbstractToolsFactory factory : toolTypeMap.values()) {
            Set<ToolCallback> callbacks = factory.getToolCallbacks(toolName);
            if (!callbacks.isEmpty()) {
                return java.util.Optional.of(callbacks.iterator().next());
            }
        }
        return java.util.Optional.empty();
    }
}
