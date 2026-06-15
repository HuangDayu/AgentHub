package com.agenthub.infrastructure.agents.spring.advisor;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.RetrievalStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * 检索策略 Advisor，在推理前执行 RetrievalStrategy 查询改写。
 */
@RequiredArgsConstructor
public class RetrievalStrategyAdvisor implements BaseAdvisor {

    private final ReActAgentContext context;

    private final RetrievalStrategy strategy;

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        Prompt prompt = request.prompt();
        String userText = getUserText(prompt);
        String rewritten = strategy.beforeRetrieval(context, userText);
        Prompt newPrompt = prompt.augmentUserMessage(msg -> new UserMessage(rewritten));
        return request.mutate().prompt(newPrompt).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    @Override
    public String getName() {
        return "RetrievalStrategyAdvisor";
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private String getUserText(Prompt prompt) {
        return prompt.getUserMessage() != null ? prompt.getUserMessage().getText() : "";
    }
}
