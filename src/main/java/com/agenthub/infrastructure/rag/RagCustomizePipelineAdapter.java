package com.agenthub.infrastructure.rag;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.port.out.rag.RagModelChatPort;
import com.agenthub.application.port.out.rag.RagRetrievalPort;
import com.agenthub.application.port.out.rag.RetrievalAugmentedGenerationPort;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.domain.model.ChatMessage;
import com.agenthub.domain.model.RetrievalChunk;
import com.agenthub.domain.model.SessionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.agenthub.domain.enums.AgentConfigCategory.MODEL;
import static com.agenthub.domain.enums.AgentConfigType.CHAT_MODEL;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agenthub.rag.impl-type", havingValue = "customize")
public class RagCustomizePipelineAdapter implements RetrievalAugmentedGenerationPort {
    private final RagRetrievalPort ragRetrievalPort;
    private final RagModelChatPort ragModelChatPort;
    private final MessagePromptBuilder messagePromptBuilder;
    private final AgentConfigRepository agentConfigRepository;


    @Override
    public List<RetrievalChunk> ragRetrieve(RagCommand ragCommand) {
        return ragRetrievalPort.retrieve(ragCommand);
    }

    @Override
    public String ragChat(RagCommand ragCommand) {
        return ragModelChatPort.chat(buildSessionMessage(ragCommand));
    }

    @Override
    public Flux<String> ragStream(RagCommand ragCommand) {
        return ragModelChatPort.stream(buildSessionMessage(ragCommand));
    }

    private SessionMessage buildSessionMessage(RagCommand ragCommand) {
        String modelId = agentConfigRepository.getConfigId(ragCommand.getAgentId(), MODEL, CHAT_MODEL);
        List<ChatMessage> chatMessages = buildMessages(ragCommand);
        return new SessionMessage(ragCommand.getSessionId(), ragCommand.getAgentId(), modelId, ragCommand.getModelStrategy(), chatMessages);
    }

    public List<ChatMessage> buildMessages(RagCommand ragCommand) {
        List<RetrievalChunk> contexts = ragRetrievalPort.retrieve(ragCommand);
        return messagePromptBuilder.build(ragCommand.getSessionId(), ragCommand.getPromptTemplate(), ragCommand.getPrompt(), contexts);
    }


}
