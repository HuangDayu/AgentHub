package com.agenthub.infrastructure.agents.spring.advisor;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.ModelStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

/**
 * 模型策略 Advisor，将 ModelStrategy 参数注入模型调用。
 */
@RequiredArgsConstructor
public class ModelStrategyAdvisor implements BaseAdvisor {

    private final ReActAgentContext context;

    private final ModelStrategy strategy;

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
        return "ModelStrategyAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
