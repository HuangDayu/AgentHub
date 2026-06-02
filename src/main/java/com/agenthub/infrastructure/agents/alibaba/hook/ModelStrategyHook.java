package com.agenthub.infrastructure.agents.alibaba.hook;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.ModelStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 模型策略 Hook — 在模型推理前后执行 ModelStrategy 生命周期钩子。
 */
@HookPositions({HookPosition.BEFORE_MODEL, HookPosition.AFTER_MODEL})
@RequiredArgsConstructor
public class ModelStrategyHook extends ModelHook {

    private final ReActAgentContext context;

    @Override
    public String getName() {
        return "ModelStrategyHook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state,
                                                              RunnableConfig config) {
        ModelStrategy strategy = context.getModelStrategy();
        strategy.beforeInference(context, List.of());
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state,
                                                             RunnableConfig config) {
        return CompletableFuture.completedFuture(Map.of());
    }
}
