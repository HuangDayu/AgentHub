package com.agenthub.infrastructure.tools;

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
 * @author huangdayu
 */
@RequiredArgsConstructor
@Component
public class AgentToolsFactory {

    private final List<AbstractToolsFactory> abstractToolsFactory;
    private static final Map<AgentToolType, AbstractToolsFactory> TOOL_TYPE_MAP = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        abstractToolsFactory.forEach(factory -> TOOL_TYPE_MAP.put(factory.getToolInfo(), factory));
    }

    public Set<ToolCallback> getToolCallback(AgentToolType toolType, String toolName) {
        return TOOL_TYPE_MAP.get(toolType).getToolCallbacks(toolName);
    }

    public Set<ToolCallback> getToolCallbacks(AgentToolType toolType, List<AgentToolInfo> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return Set.of();
        }
        return TOOL_TYPE_MAP.get(toolType).getToolCallbacks(toolIds.stream()
                .filter(toolInfo -> toolInfo.getType() == toolType).toList());
    }


}
