package com.agenthub.application.usecase;

import com.agenthub.domain.model.strategy.ToolStrategy;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工具过滤用例，根据 ToolStrategy 动态过滤工具列表。
 */
@Component
public class ToolFilterUseCase {

    public Set<ToolCallback> filterByStrategy(Set<ToolCallback> allCallbacks, ToolStrategy strategy) {
        if (strategy == null || strategy.getToolBindings() == null) {
            return allCallbacks;
        }
        if (strategy.getToolBindings().isEmpty()) return allCallbacks;

        List<String> enabledTools = strategy.getToolBindings().stream()
                .filter(binding -> binding.isEnabled())
                .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .map(binding -> binding.getToolId())
                .collect(Collectors.toList());

        if (enabledTools.isEmpty()) return allCallbacks;

        return allCallbacks.stream()
                .filter(cb -> isEnabled(cb, enabledTools))
                .collect(Collectors.toSet());
    }

    private boolean isEnabled(ToolCallback callback, List<String> enabledTools) {
        String name = callback.getToolDefinition().name();
        return enabledTools.contains(name);
    }
}
