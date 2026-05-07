package com.agenthub.infrastructure.tools;

import com.agenthub.domain.model.AgentToolType;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@Component
public class AgentToolsFactory {

    private final List<AbstractToolsFactory> abstractToolsFactory;

    public Set<ToolCallback> getToolCallback(AgentToolType toolType, String toolName) {
        return abstractToolsFactory.stream()
                .filter(toolCallback -> toolType.equals(toolCallback.getToolInfo().getType()))
                .findFirst().orElseThrow().getToolCallbacks(toolName);
    }

    public Set<ToolCallback> getToolCallbacks(AgentToolType toolType, List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return Set.of();
        }
        return abstractToolsFactory.stream()
                .filter(toolCallback -> toolType.equals(toolCallback.getToolInfo().getType()))
                .findFirst().orElseThrow().getToolCallbacks(toolIds);
    }

    public Set<ToolCallback> getToolCallbacks() {
        return abstractToolsFactory.stream()
                .map(AbstractToolsFactory::getAllToolCallbacks)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
}
