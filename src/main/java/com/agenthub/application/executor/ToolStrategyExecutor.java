package com.agenthub.application.executor;

import com.agenthub.application.port.out.ToolInvocationPort;
import com.agenthub.domain.model.ToolStrategy;
import com.agenthub.domain.model.ToolStrategy.ToolBinding;
import com.agenthub.application.command.InvokeToolCommand;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具策略执行器 - 管理和执行工具
 */
@Component
@AllArgsConstructor
public class ToolStrategyExecutor {
    private final ToolInvocationPort toolInvocationPort;

    public List<ToolInfo> getAvailableTools(ToolStrategy strategy) {
        List<ToolBinding> bindings = strategy.getToolBindings();
        return bindings.stream()
                .map(this::toToolInfo)
                .collect(Collectors.toList());
    }

    public ToolExecutionResult executeTool(
            ToolStrategy strategy,
            String toolId,
            Map<String, Object> params
    ) {
        ToolBinding binding = findBinding(strategy, toolId);
        if (binding == null) return ToolExecutionResult.notFound(toolId);
        if (!binding.isEnabled()) return ToolExecutionResult.disabled(toolId);
        return invokeTool(toolId, params);
    }

    private ToolBinding findBinding(ToolStrategy strategy, String toolId) {
        return strategy.getToolBindings().stream()
                .filter(b -> b.getToolId().equals(toolId))
                .findFirst()
                .orElse(null);
    }

    private ToolExecutionResult invokeTool(String toolId, Map<String, Object> params) {
        try {
            var result = toolInvocationPort.invokeTool(toolId, new InvokeToolCommand(null, params));
            return ToolExecutionResult.success(toolId, result.output());
        } catch (Exception e) {
            return ToolExecutionResult.error(toolId, e.getMessage());
        }
    }

    private ToolInfo toToolInfo(ToolBinding binding) {
        return new ToolInfo(
                binding.getToolId(),
                binding.isEnabled(),
                binding.getPriority()
        );
    }

    public record ToolInfo(
            String toolId,
            boolean enabled,
            int executionOrder
    ) {
    }

    public record ToolExecutionResult(
            String toolId,
            boolean success,
            Map<String, Object> output,
            String error
    ) {
        public static ToolExecutionResult success(String toolId, Map<String, Object> output) {
            return new ToolExecutionResult(toolId, true, output, null);
        }

        public static ToolExecutionResult notFound(String toolId) {
            return new ToolExecutionResult(toolId, false, null, "Tool not found");
        }

        public static ToolExecutionResult disabled(String toolId) {
            return new ToolExecutionResult(toolId, false, null, "Tool is disabled");
        }

        public static ToolExecutionResult error(String toolId, String error) {
            return new ToolExecutionResult(toolId, false, null, error);
        }
    }
}
