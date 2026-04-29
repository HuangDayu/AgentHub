package com.agenthub.application.usecase;

import com.agenthub.application.dto.ValidationOutput;
import com.agenthub.application.executor.GuardrailStrategyExecutor;
import com.agenthub.application.executor.ModelStrategyExecutor;
import com.agenthub.application.executor.RetrievalStrategyExecutor;
import com.agenthub.application.executor.ToolStrategyExecutor;
import com.agenthub.domain.model.*;
import com.agenthub.domain.model.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 策略执行器 - 统一调度所有策略执行
 */
@Component
public class StrategyUseCase {
    private final RetrievalStrategyExecutor retrievalExecutor;
    private final ModelStrategyExecutor modelExecutor;
    private final ToolStrategyExecutor toolExecutor;
    private final GuardrailStrategyExecutor guardrailExecutor;
    private final RetrievalStrategyUseCase retrievalUseCase;
    private final ModelStrategyUseCase modelUseCase;
    private final ToolStrategyUseCase toolUseCase;
    private final GuardrailStrategyUseCase guardrailUseCase;

    public StrategyUseCase(
            RetrievalStrategyExecutor retrievalExecutor,
            ModelStrategyExecutor modelExecutor,
            ToolStrategyExecutor toolExecutor,
            GuardrailStrategyExecutor guardrailExecutor,
            RetrievalStrategyUseCase retrievalUseCase,
            ModelStrategyUseCase modelUseCase,
            ToolStrategyUseCase toolUseCase,
            GuardrailStrategyUseCase guardrailUseCase
    ) {
        this.retrievalExecutor = retrievalExecutor;
        this.modelExecutor = modelExecutor;
        this.toolExecutor = toolExecutor;
        this.guardrailExecutor = guardrailExecutor;
        this.retrievalUseCase = retrievalUseCase;
        this.modelUseCase = modelUseCase;
        this.toolUseCase = toolUseCase;
        this.guardrailUseCase = guardrailUseCase;
    }

    public List<RetrievalChunk> executeRetrieval(String strategyId, List<String> kbIds, String query) {
        if (strategyId == null) return List.of();
        RetrievalStrategy strategy = retrievalUseCase.get(strategyId);
        return retrievalExecutor.execute(strategy, kbIds, query);
    }

    public String chatModel(String modelId, String strategyId, String agentId, String sessionId, List<ChatMessage> chatMessages) {
        if (strategyId == null) return "未配置模型策略";
        ModelStrategy strategy = modelUseCase.get(strategyId);
        return modelExecutor.execute(new SessionMessage(sessionId, agentId, modelId, strategy, chatMessages));
    }

    public Flux<String> streamModel(String modelId, String strategyId, String agentId, String sessionId, List<ChatMessage> chatMessages) {
        if (strategyId == null) return Flux.create(sink -> sink.error(new Exception("未配置模型策略")));
        ModelStrategy strategy = modelUseCase.get(strategyId);
        return modelExecutor.stream(new SessionMessage(sessionId, agentId, modelId, strategy, chatMessages));
    }

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
