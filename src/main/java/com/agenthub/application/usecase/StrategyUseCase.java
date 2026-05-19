package com.agenthub.application.usecase;

import com.agenthub.application.dto.ValidationOutput;
import com.agenthub.application.executor.GuardrailStrategyExecutor;
import com.agenthub.domain.model.strategy.GuardrailStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 策略执行器 - 统一调度所有策略执行
 */
@Component
@RequiredArgsConstructor
public class StrategyUseCase {
    private final GuardrailStrategyExecutor guardrailExecutor;
    private final RetrievalStrategyUseCase retrievalUseCase;
    private final ModelStrategyUseCase modelUseCase;
    private final ToolStrategyUseCase toolUseCase;
    private final GuardrailStrategyUseCase guardrailUseCase;


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
