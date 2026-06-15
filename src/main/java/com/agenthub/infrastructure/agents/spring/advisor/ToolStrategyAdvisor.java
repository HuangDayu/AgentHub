package com.agenthub.infrastructure.agents.spring.advisor;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.ToolStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

/**
 * 工具策略 Advisor，在工具调用前后执行 ToolStrategy 生命周期钩子。
 */
@RequiredArgsConstructor
public class ToolStrategyAdvisor implements BaseAdvisor {

    private final ReActAgentContext context;

    private final ToolStrategy strategy;

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    @Override
    public String getName() {
        return "ToolStrategyAdvisor";
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
