package com.agenthub.infrastructure.agents.aliyun.hook;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.RetrievalStrategy;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreCallEvent;
import reactor.core.publisher.Mono;

/**
 * 检索策略 Hook — 在 AgentScope 检索前执行 RetrievalStrategy 生命周期钩子。
 */
public class RetrievalStrategyHook implements Hook {

    private final ReActAgentContext context;

    /**
     * 构造检索策略 Hook。
     *
     * @param context Agent 上下文
     */
    public RetrievalStrategyHook(ReActAgentContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PreCallEvent preCall) {
            handleBeforeRetrieval(preCall);
        }
        return Mono.just(event);
    }

    /** 处理检索前事件。 */
    private void handleBeforeRetrieval(PreCallEvent event) {
        RetrievalStrategy strategy = context.getRetrievalStrategy();
        String query = extractQuery(event);
        strategy.beforeRetrieval(context, query);
    }

    /** 从事件中提取查询。 */
    private String extractQuery(PreCallEvent event) {
        return event.getInputMessages() != null
                ? event.getInputMessages().toString()
                : "";
    }

    @Override
    public int priority() {
        return 60;
    }
}
