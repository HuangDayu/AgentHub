package com.agenthub.application.usecase;

import com.agenthub.application.dto.AgentOutput;
import com.agenthub.application.dto.MessageOutput;
import com.agenthub.application.dto.ValidationOutput;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.application.port.out.repositories.PromptTemplateRepository;
import com.agenthub.application.port.out.repositories.StudioSessionRepository;
import com.agenthub.application.service.MessagesPipelineService;
import com.agenthub.domain.model.PromptTemplate;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.ChatMessage;
import com.agenthub.domain.model.RetrievalChunk;
import com.agenthub.domain.model.Session;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.agenthub.common.utils.RandomUtils.randomId;
import static com.agenthub.domain.model.AgentConfig.Category.*;
import static com.agenthub.domain.model.AgentConfig.Type.*;

@Component
public class ChatUseCase {
    private final AgentUseCase agentUseCase;
    private final StrategyUseCase strategyUseCase;
    private final AgentConfigRepository configRepo;
    private final MessagesPipelineService messagesPipelineService;
    private final StudioSessionRepository studioSessionRepository;
    private final PromptTemplateRepository promptTemplateRepository;

    public ChatUseCase(AgentUseCase agentUseCase, StrategyUseCase strategyUseCase,
                       AgentConfigRepository configRepo, MessagesPipelineService messagesPipelineService,
                       StudioSessionRepository studioSessionRepository, PromptTemplateRepository promptTemplateRepository) {
        this.agentUseCase = agentUseCase;
        this.strategyUseCase = strategyUseCase;
        this.configRepo = configRepo;
        this.messagesPipelineService = messagesPipelineService;
        this.studioSessionRepository = studioSessionRepository;
        this.promptTemplateRepository = promptTemplateRepository;
    }

    public MessageOutput sendChat(String agentId, String sessionId, String userPrompt) {
        String message = chat(agentId, sessionId, userPrompt);
        return new MessageOutput(randomId(), sessionId, "assistant", message, Instant.now());
    }

    public Flux<String> streamChat(String agentId, String sessionId, String userPrompt) {
        AgentOutput agent = agentUseCase.get(agentId);
        validatePublished(agent);
        ValidationOutput validation = validateInput(agentId, userPrompt);
        if (!validation.valid()) return createErrorFlux(validation);
        List<RetrievalChunk> contexts = retrieveKnowledge(agentId, userPrompt);
        List<ChatMessage> messages = buildMessages(agentId, userPrompt, contexts);
        return streamModel(agentId, sessionId, messages);
    }

    public String chat(String agentId, String sessionId, String userPrompt) {
        AgentOutput agent = agentUseCase.get(agentId);
        validatePublished(agent);
        ValidationOutput inputValidation = validateInput(agentId, userPrompt);
        if (!inputValidation.valid()) return "输入验证失败";
        List<RetrievalChunk> contexts = retrieveKnowledge(agentId, userPrompt);
        List<ChatMessage> messages = buildMessages(agentId, userPrompt, contexts);
        String response = chatModel(agentId, sessionId, messages);
        ValidationOutput outputValidation = validateOutput(agentId, response);
        if (!outputValidation.valid()) return "输出验证失败";
        saveSession(sessionId, agentId, messages, response);
        return response;
    }

    private void validatePublished(AgentOutput agent) {
        if (!agent.enabled()) {
            throw new IllegalStateException("Agent未启用");
        }
    }

    private Flux<String> createErrorFlux(ValidationOutput validation) {
        return Flux.error(new IllegalStateException(String.join(",", validation.violations())));
    }

    private ValidationOutput validateInput(String agentId, String input) {
        String guardrailId = getConfigId(agentId, STRATEGY, GUARDRAIL_STRATEGY);
        return strategyUseCase.validateInput(guardrailId, input);
    }

    private ValidationOutput validateOutput(String agentId, String output) {
        String guardrailId = getConfigId(agentId, STRATEGY, GUARDRAIL_STRATEGY);
        return strategyUseCase.validateOutput(guardrailId, output);
    }

    private List<RetrievalChunk> retrieveKnowledge(String agentId, String query) {
        String retrievalId = getConfigId(agentId, STRATEGY, RETRIEVAL_STRATEGY);
        List<AgentConfig> knowledgeBases = configRepo.findEnabledAgentConfigs(agentId, KNOWLEDGE, KNOWLEDGE_BASE);
        List<String> knowledgeBaseIds = knowledgeBases.stream().map(AgentConfig::configId).collect(Collectors.toList());
        return strategyUseCase.executeRetrieval(retrievalId, knowledgeBaseIds, query);
    }

    private List<ChatMessage> buildMessages(String agentId, String userPrompt, List<RetrievalChunk> contexts) {
        String promptTemplate = getPromptTemplate(agentId);
        return messagesPipelineService.build(promptTemplate, userPrompt, contexts);
    }

    private String chatModel(String agentId, String sessionId, List<ChatMessage> messages) {
        String modelId = getConfigId(agentId, MODEL, CHAT_MODEL);
        String strategyId = getConfigId(agentId, STRATEGY, MODEL_STRATEGY);
        return strategyUseCase.chatModel(modelId, strategyId, agentId, sessionId, messages);
    }

    private Flux<String> streamModel(String agentId, String sessionId, List<ChatMessage> messages) {
        String modelId = getConfigId(agentId, MODEL, CHAT_MODEL);
        String strategyId = getConfigId(agentId, STRATEGY, MODEL_STRATEGY);
        return strategyUseCase.streamModel(modelId, strategyId, agentId, sessionId, messages);
    }

    private void saveSession(String sessionId, String agentId, List<ChatMessage> messages, String response) {
        messages.add(ChatMessage.assistant(response));
        studioSessionRepository.addMessage(new Session(sessionId, agentId, null, null, Instant.now(), messages));
    }

    private String getConfigId(String agentId, AgentConfig.Category category, AgentConfig.Type type) {
        AgentConfig config = configRepo.findOneAgentConfig(agentId, category, type);
        if (config == null) {
            throw new IllegalStateException("未找到对应的配置");
        }
        return config.configId();
    }

    private String getPromptTemplate(String agentId) {
        String configId = getConfigId(agentId, AgentConfig.Category.PROMPT, SYSTEM_PROMPT);
        Optional<PromptTemplate> optional = promptTemplateRepository.findById(configId);
        return optional.isPresent() ? optional.get().content() : " ";
    }
}
