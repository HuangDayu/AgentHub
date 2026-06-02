package com.agenthub.infrastructure.agents.aliyun.hook;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.ToolStrategy;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreActingEvent;
import io.agentscope.core.hook.PostActingEvent;
import reactor.core.publisher.Mono;

/**
 * 工具策略 Hook — 在 AgentScope 工具执行前后执行 ToolStrategy 生命周期钩子。
 */
public class ToolStrategyHook implements Hook {

    private final ReActAgentContext context;

    /**
     * 构造工具策略 Hook。
     *
     * @param context Agent 上下文
     */
    public ToolStrategyHook(ReActAgentContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PreActingEvent preActing) {
            handleBeforeToolCall(preActing);
        } else if (event instanceof PostActingEvent postActing) {
            handleAfterToolCall(postActing);
        }
        return Mono.just(event);
    }

    /** 处理工具调用前事件。 */
    private void handleBeforeToolCall(PreActingEvent event) {
        ToolStrategy strategy = context.getToolStrategy();
        String toolName = extractToolName(event);
        strategy.beforeToolCall(context, toolName, "");
    }

    /** 处理工具调用后事件。 */
    private void handleAfterToolCall(PostActingEvent event) {
        ToolStrategy strategy = context.getToolStrategy();
        String toolName = extractToolName(event);
        strategy.afterToolCall(context, toolName, "");
    }

    /** 从事件中提取工具名称。 */
    private String extractToolName(PreActingEvent event) {
        return event.getToolUse() != null ? event.getToolUse().getName() : "";
    }

    /** 从事件中提取工具名称。 */
    private String extractToolName(PostActingEvent event) {
        return event.getToolUse() != null ? event.getToolUse().getName() : "";
    }

    @Override
    public int priority() {
        return 50;
    }
}
