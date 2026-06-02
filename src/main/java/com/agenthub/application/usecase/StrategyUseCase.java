package com.agenthub.application.usecase;

import com.agenthub.domain.model.strategy.GuardrailStrategy;
import com.agenthub.domain.model.strategy.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 策略执行器 — 统一调度所有策略执行。
 */
@Component
@RequiredArgsConstructor
public class StrategyUseCase {

    private final RetrievalStrategyUseCase retrievalUseCase;
    private final ModelStrategyUseCase modelUseCase;
    private final ToolStrategyUseCase toolUseCase;
    private final GuardrailStrategyUseCase guardrailUseCase;

    /**
     * 验证用户输入。
     *
     * @param strategyId 策略ID
     * @param input 用户输入
     * @return 验证结果
     */
    public ValidationResult validateInput(String strategyId, String input) {
        if (strategyId == null) return ValidationResult.pass();
        GuardrailStrategy strategy = guardrailUseCase.get(strategyId);
        return strategy.validateInput(input);
    }

    /**
     * 验证模型输出。
     *
     * @param strategyId 策略ID
     * @param output 模型输出
     * @return 验证结果
     */
    public ValidationResult validateOutput(String strategyId, String output) {
        if (strategyId == null) return ValidationResult.pass();
        GuardrailStrategy strategy = guardrailUseCase.get(strategyId);
        return strategy.validateOutput(output);
    }
}
