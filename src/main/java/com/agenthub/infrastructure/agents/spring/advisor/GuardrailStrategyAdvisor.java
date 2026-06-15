package com.agenthub.infrastructure.agents.spring.advisor;

import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.strategy.GuardrailStrategy;
import com.agenthub.domain.model.strategy.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * 护栏策略 Advisor，在推理前后执行 GuardrailStrategy 输入输出校验。
 */
@RequiredArgsConstructor
public class GuardrailStrategyAdvisor implements BaseAdvisor {

    private final ReActAgentContext context;

    private final GuardrailStrategy strategy;

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        validateInput(request.prompt());
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        if (strategy.isOutputValidationEnabled() && response.chatResponse() != null) {
            String text = response.chatResponse().getResult().getOutput().getText();
            ValidationResult result = strategy.validateOutput(text);
            throwIfInvalid(result);
        }
        return response;
    }

    @Override
    public String getName() {
        return "GuardrailStrategyAdvisor";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private void validateInput(Prompt prompt) {
        if (!strategy.isInputValidationEnabled()) return;
        String userText = prompt.getUserMessage() != null ? prompt.getUserMessage().getText() : "";
        ValidationResult result = strategy.validateInput(userText);
        throwIfInvalid(result);
    }

    private void throwIfInvalid(ValidationResult result) {
        if (!result.isValid()) {
            throw new ValidationException(String.join("; ", result.getViolations()));
        }
    }
}
