package com.agenthub.application.usecase;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.dto.AgentOutput;
import com.agenthub.application.dto.MessageOutput;
import com.agenthub.application.dto.ValidationOutput;
import com.agenthub.application.port.out.rag.RetrievalAugmentedGenerationPort;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.agenthub.common.utils.RandomUtils.randomId;
import static com.agenthub.domain.model.AgentConfigCategory.KNOWLEDGE;
import static com.agenthub.domain.model.AgentConfigCategory.STRATEGY;
import static com.agenthub.domain.model.AgentConfigType.*;


@Component
@RequiredArgsConstructor
public class RagChatUseCase {
    private final AgentUseCase agentUseCase;
    private final StrategyUseCase strategyUseCase;
    private final AgentConfigRepository agentConfigRepository;
    private final SessionRepository sessionRepository;
    private final RetrievalAugmentedGenerationPort retrievalAugmentedGenerationPort;
    private final RetrievalStrategyRepository retrievalStrategyRepository;
    private final ModelStrategyRepository modelStrategyRepository;
    private final PromptTemplateRepository promptTemplateRepository;

    public MessageOutput sendChat(String agentId, String sessionId, String userPrompt) {
        String message = chat(agentId, sessionId, userPrompt);
        return new MessageOutput(randomId(), sessionId, "assistant", message, Instant.now());
    }

    public Flux<String> streamChat(String agentId, String sessionId, String userPrompt) {
        AgentOutput agent = agentUseCase.get(agentId);
        validatePublished(agent);
        ValidationOutput validation = validateInput(agentId, userPrompt);
        if (!validation.isValid()) return createErrorFlux(validation);
        return retrievalAugmentedGenerationPort.ragStream(buildRagCommand(agentId, sessionId, userPrompt));
    }

    public String chat(String agentId, String sessionId, String userPrompt) {
        AgentOutput agent = agentUseCase.get(agentId);
        validatePublished(agent);
        ValidationOutput validation = validateInput(agentId, userPrompt);
        if (!validation.isValid()) return "输入验证失败";
        ArrayList<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.user(sessionId, userPrompt));
        String response = retrievalAugmentedGenerationPort.ragChat(buildRagCommand(agentId, sessionId, userPrompt));
        ValidationOutput outputValidation = validateOutput(agentId, response);
        if (!outputValidation.isValid()) return "输出验证失败";
        messages.add(ChatMessage.assistant(sessionId, response));
        saveSession(sessionId, agentId, messages, response);
        return response;
    }

    private RagCommand buildRagCommand(String agentId, String sessionId, String userPrompt) {
        return new RagCommand(sessionId, agentId, userPrompt, getKnowledgeBaseIds(agentId), retrievalStrategy(agentId), modelStrategy(agentId), getPromptTemplate(agentId));
    }

    private List<String> getKnowledgeBaseIds(String agentId) {
        List<AgentConfig> knowledgeBases = agentConfigRepository.findEnabledAgentConfigs(agentId, KNOWLEDGE, KNOWLEDGE_BASE);
        return knowledgeBases.stream().map(AgentConfig::getConfigId).collect(Collectors.toList());
    }

    private ModelStrategy modelStrategy(String agentId) {
        String modelStrategyId = agentConfigRepository.getConfigId(agentId, STRATEGY, MODEL_STRATEGY);
        return modelStrategyRepository.findById(modelStrategyId).orElseThrow(() -> new NotFoundException("ModelStrategy not found: " + modelStrategyId));
    }

    private RetrievalStrategy retrievalStrategy(String agentId) {
        String retrievalId = agentConfigRepository.getConfigId(agentId, STRATEGY, RETRIEVAL_STRATEGY);
        return retrievalStrategyRepository.findById(retrievalId).orElseThrow(() -> new NotFoundException("RetrievalStrategy not found: " + retrievalId));
    }

    private String getPromptTemplate(String agentId) {
        String configId = agentConfigRepository.getConfigId(agentId, AgentConfigCategory.PROMPT, SYSTEM_PROMPT);
        Optional<PromptTemplateInfo> optional = promptTemplateRepository.findById(configId);
        return optional.isPresent() ? optional.get().getContent() : " ";
    }

    private void validatePublished(AgentOutput agent) {
        if (!agent.isEnabled()) {
            throw new IllegalStateException("Agent未启用");
        }
    }

    private Flux<String> createErrorFlux(ValidationOutput validation) {
        return Flux.error(new IllegalStateException(String.join(",", validation.getViolations())));
    }

    private ValidationOutput validateInput(String agentId, String input) {
        String guardrailId = agentConfigRepository.getConfigId(agentId, STRATEGY, GUARDRAIL_STRATEGY);
        return strategyUseCase.validateInput(guardrailId, input);
    }

    private ValidationOutput validateOutput(String agentId, String output) {
        String guardrailId = agentConfigRepository.getConfigId(agentId, STRATEGY, GUARDRAIL_STRATEGY);
        return strategyUseCase.validateOutput(guardrailId, output);
    }


    private void saveSession(String sessionId, String agentId, List<ChatMessage> messages, String response) {
        sessionRepository.saveMessages(messages);
    }

}
