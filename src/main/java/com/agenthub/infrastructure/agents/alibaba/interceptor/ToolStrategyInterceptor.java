package com.agenthub.infrastructure.agents.alibaba.interceptor;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.ToolStrategy;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import lombok.RequiredArgsConstructor;

/**
 * 工具策略拦截器 — 在工具调用前后执行 ToolStrategy 生命周期钩子。
 */
@RequiredArgsConstructor
public class ToolStrategyInterceptor extends ToolInterceptor {

    private final ReActAgentContext context;

    @Override
    public String getName() {
        return "ToolStrategyInterceptor";
    }

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request,
                                              ToolCallHandler handler) {
        ToolStrategy strategy = context.getToolStrategy();
        strategy.beforeToolCall(context, request.getToolName(), "");
        ToolCallResponse response = handler.call(request);
        strategy.afterToolCall(context, request.getToolName(), response.getResult());
        return response;
    }
}
