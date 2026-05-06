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

    public Set<ToolCallback> getToolCallbacks(AgentToolType toolType) {
        return abstractToolsFactory.stream()
                .filter(toolCallback -> toolType.equals(toolCallback.getToolInfo().getType()))
                .findFirst().orElseThrow().getToolCallbacks();
    }

    public Set<ToolCallback> getToolCallbacks() {
        return abstractToolsFactory.stream()
                .map(AbstractToolsFactory::getToolCallbacks)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
}
