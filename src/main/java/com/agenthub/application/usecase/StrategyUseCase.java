package com.agenthub.application.usecase;

import com.agenthub.application.dto.ValidationOutput;
import com.agenthub.application.executor.GuardrailStrategyExecutor;
import com.agenthub.application.executor.ToolStrategyExecutor;
import com.agenthub.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 策略执行器 - 统一调度所有策略执行
 */
@Component
@RequiredArgsConstructor
public class StrategyUseCase {
    private final ToolStrategyExecutor toolExecutor;
    private final GuardrailStrategyExecutor guardrailExecutor;
    private final RetrievalStrategyUseCase retrievalUseCase;
    private final ModelStrategyUseCase modelUseCase;
    private final ToolStrategyUseCase toolUseCase;
    private final GuardrailStrategyUseCase guardrailUseCase;

    public List<ToolStrategyExecutor.ToolInfo> getTools(String strategyId) {
        if (strategyId == null) return List.of();
        ToolStrategy strategy = toolUseCase.get(strategyId);
        return toolExecutor.getAvailableTools(strategy);
    }

    public ValidationOutput validateInput(
            String strategyId, String input
    ) {
        if (strategyId == null) return GuardrailStrategyExecutor.valid();
        GuardrailStrategy strategy = guardrailUseCase.get(strategyId);
        return guardrailExecutor.validateInput(strategy, input);
    }

    public ValidationOutput validateOutput(
            String strategyId, String output
    ) {
        if (strategyId == null) return GuardrailStrategyExecutor.valid();
        GuardrailStrategy strategy = guardrailUseCase.get(strategyId);
        return guardrailExecutor.validateOutput(strategy, output);
    }
}
